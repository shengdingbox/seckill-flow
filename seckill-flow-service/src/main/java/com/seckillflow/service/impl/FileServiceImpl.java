package com.seckillflow.service.impl;


import com.seckillflow.controller.scheduler.request.BatchFileQueryRequest;
import com.seckillflow.controller.scheduler.request.FileDetailQueryRequest;
import com.seckillflow.domain.dto.FileDetailDTO;
import com.seckillflow.domain.model.BatchFileReg;
import com.seckillflow.mapper.FileMapper;
import com.seckillflow.service.FileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 周子斐
 * @date 2021/9/22
 * @Description
 */
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileMapper fileMapper;
    @Value("${file.base.path}")
    private String basePath;

    @Override
    public List<BatchFileReg> findByDateAndSource(BatchFileQueryRequest request) {
        String filePath = request.getFilePath();
        filePath = StringUtils.isBlank(filePath) ? "" : filePath;
        final File file = new File(basePath + File.separator + filePath);
        final File[] files = file.listFiles();
        List<BatchFileReg> batchFileRegs = new ArrayList<>();
        for (File file1 : files) {
            final String name = file1.getName();
            final String absolutePath = file.getAbsolutePath();
            final boolean directory = file1.isDirectory();
            final String time = getTime(absolutePath);
            final long length = file1.length();
            final BatchFileReg batchFileReg = new BatchFileReg();
            batchFileReg.setFileName(name);
            batchFileReg.setPathFileName(absolutePath);
            batchFileReg.setDownloadTime(time);
            batchFileReg.setDataType(directory ? "文件夹" : "文件");
            batchFileReg.setLength(String.valueOf(length));
            batchFileRegs.add(batchFileReg);
        }
        return batchFileRegs;

    }

    @Override
    public FileDetailDTO detail(FileDetailQueryRequest request) {
        final String pathFileName = request.getPathFileName();
        final String fileName = request.getFileName();
        final String status = request.getStatus();
        String filePath = basePath + File.separator + pathFileName + File.separator + fileName;
        StringBuilder sb = new StringBuilder();
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            Charset charset = detectCharset(filePath);
            sb.append(readFileToString(filePath, StringUtils.isBlank(status) ? "GBK" : status));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final FileDetailDTO fileDetailDTO = new FileDetailDTO();
        fileDetailDTO.setResponseBody(sb.toString());
        fileDetailDTO.setId(1L);
        return fileDetailDTO;
    }

    public String getTime(String absolutePath) {
        Path path = Paths.get(absolutePath);
        // 根据path获取文件的基本属性类
        BasicFileAttributes attrs = null;
        try {
            attrs = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 从基本属性类中获取文件创建时间
        FileTime fileTime = attrs.creationTime();
        // 将文件创建时间转成毫秒
        long millis = fileTime.toMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        Date date = new Date();
        date.setTime(millis);
        // 毫秒转成时间字符串
        String time = dateFormat.format(date);
        return time;
    }

    private static Charset detectCharset(String path) throws IOException {
        Charset[] charsets = {StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1, Charset.forName("GBK")};

        for (Charset charset : charsets) {
            InputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(path)));
            byte[] bytes = new byte[4096];
            int length;
            boolean valid = true;
            while ((length = in.read(bytes)) != -1) {
                String str = new String(bytes, 0, length, charset);
                valid = str.chars().allMatch(c -> c < 128 || c == '\n' || c == '\r');
                if (!valid) {
                    break;
                }
            }
            in.close();

            if (valid) {
                return charset;
            }
        }

        return StandardCharsets.UTF_8;
    }

    public static String readFileToString(String filePath, String encoding) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(filePath)), encoding))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
