package com.seckillflow.mapper;


import org.apache.ibatis.annotations.Mapper;

/**
 * @author 周子斐
 * @date 2021/9/22
 * @Description
 */
@Mapper
public interface FileMapper {

//    /**
//     * 新增批量文件注册信息
//     *
//     * @param batchFileReg batchFileReg
//     */
//    @Override
//    public void saveBatchFileReg(BatchFileReg batchFileReg) {
//        String sql = "INSERT INTO batch_file_reg (methods,file_name,path_file_name,source,status,download_time,data_type,job_name,data_time) " +
//                "VALUES (:methods,:fileName,:pathFileName,:source,:status,:downloadTime,:dataType,:jobName,:dataTime)";
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        jdbc.update(sql, new BeanPropertySqlParameterSource(batchFileReg), keyHolder);
//        List<Map<String, Object>> keyList = keyHolder.getKeyList();
//        Map<String, Object> stringObjectMap = keyList.stream().findFirst().orElseThrow(RuntimeException::new);
//
//        batchFileReg.setId((Integer) stringObjectMap.get("id"));
//    }
//
//    @Override
//    public BatchFileReg findById(Integer id) {
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("id", id);
//        return jdbc.queryForObject("select * from batch_file_reg where id=:id", params, BeanPropertyRowMapper.newInstance(BatchFileReg.class));
//    }
//
//    @Override
//    public void updateStatus(Integer id, String status) {
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("id", id);
//        params.put("status", status);
//        jdbc.update("update batch_file_reg set status=:status where id=:id", params);
//    }
//    /**
//     * 查询已下载和执行失败的文件
//     *
//     * @param source   来源系统编号
//     * @param dataType 数据类型
//     * @param dataTime 数据日期
//     * @return
//     */
//    @Override
//    public List<BatchFileReg> findForJobByDate(String source, String dataType, String dataTime) {
//        StringBuilder sql = new StringBuilder("SELECT * FROM batch_file_reg WHERE methods = '0' AND status in('0','3') ");
//        sql.append(" AND data_type=:dataType AND source=:source AND data_time=:dataTime ");
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("dataType", dataType);
//        params.put("source", source);
//        params.put("dataTime", dataTime);
//        List<BatchFileReg> files = jdbc.query(sql.toString(), params, BeanPropertyRowMapper.newInstance(BatchFileReg.class));
//        return files;
//    }
//
//    @Override
//    public void updateBathFileReg(Integer id, String status, String jobName) {
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("id", id);
//        params.put("status", status);
//        params.put("jobName", jobName);
//        jdbc.update("UPDATE batch_file_reg SET STATUS = :status,JOB_NAME=:jobName WHERE ID = :id ", params);
//    }
//
//    @Override
//    public List<BatchFileReg> findBefore(int days) {
//        Date date = Dates.addDays(new Date(), -days);
//        String sql = "select * from batch_file_reg where download_time<:date and status !='4' ";
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("date", date);
//        return jdbc.query(sql, params, BeanPropertyRowMapper.newInstance(BatchFileReg.class));
//    }
}
