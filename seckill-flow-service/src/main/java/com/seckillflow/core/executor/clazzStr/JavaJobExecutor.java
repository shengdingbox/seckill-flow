package com.seckillflow.core.executor.clazzStr;

import com.alibaba.fastjson.JSONObject;
import com.seckillflow.core.executor.JobExecution;
import com.seckillflow.core.executor.JobExecutor;
import com.seckillflow.domain.entity.TaskJobEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class JavaJobExecutor implements JobExecutor {

    public static void main(String[] args) {

        String sourceCode =
                "import java.util.*;" +
                        "public class TempClass {\n" +
                        "    public static List<String> main(String[] args) {\n" +
                        "        for (String arg : args) {\n" +
                        "            System.out.println(arg);\n" +
                        "        }\n" +
                        "ArrayList<String> strings = new ArrayList<>();\n" +
                        "     return strings;\n" +
                        "    }\n" +
                        "}\n";
        begin(sourceCode, args);
    }

    @Override
    public JobExecution execute(TaskJobEntity taskJobEntity) throws Exception {
        if (taskJobEntity == null) {
            return null;
        }
        String classStr = taskJobEntity.getScriptCommand();
        String begin = begin(classStr, new String[]{"1", "2", "3"});
        JobExecution jobExecution = new JobExecution();
        jobExecution.setResult(begin);
        return jobExecution;
    }

    public static String begin(String sourceCode, String[] args) {
        Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");
        Matcher matcher = CLASS_PATTERN.matcher(sourceCode);
        if (!matcher.find()) {
            System.out.println("No class found.");
        }
        String className = matcher.group(1);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.out.println("Java compiler not found.");
            return null;
        }
        SimpleJavaFileObject file = new InMemoryJavaFileObject(className, sourceCode);
        JavaFileManager fileManager = new MemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));
        boolean success = compiler.getTask(null, fileManager, null, null, null, Arrays.asList(file)).call();
        if (success) {
            try {
                ClassLoader classLoader = new MemoryClassLoader(((MemoryJavaFileManager) fileManager).getClassBytes());
                Class<?> cls = classLoader.loadClass(className);
                Method mainMethod = cls.getMethod("main", String[].class);
                Object invoke = mainMethod.invoke(null, (Object) args);
                return JSONObject.parseObject(invoke.toString()).toJSONString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.error("Compilation failed.");
            return null;
        }
        return null;
    }

    static class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private final String code;

        protected InMemoryJavaFileObject(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    static class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Map<String, byte[]> classBytes = new HashMap<>();

        protected MemoryJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            return new MemoryJavaFileObject(className, kind);
        }

        byte[] getClassBytes(String className) {
            return classBytes.get(className);
        }

        Map<String, byte[]> getClassBytes() {
            return classBytes;
        }

        private class MemoryJavaFileObject extends SimpleJavaFileObject {
            private final String className;

            MemoryJavaFileObject(String className, Kind kind) {
                super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
                this.className = className;
            }

            @Override
            public OutputStream openOutputStream() throws IOException {
                return new ByteArrayOutputStream() {
                    @Override
                    public void close() throws IOException {
                        classBytes.put(className, toByteArray());
                        super.close();
                    }
                };
            }
        }
    }

    static class MemoryClassLoader extends ClassLoader {
        private final Map<String, byte[]> classBytes;

        MemoryClassLoader(Map<String, byte[]> classBytes) {
            this.classBytes = classBytes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classBytes.get(name);
            if (bytes == null) {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
