import request from '@/utils/request'

const SCHEDULER_URL_PRE = '/v1/jobs' // 调度器公共路径

// 查询所有群组
export function queryAllJobs(query) {
  return request({
    url: SCHEDULER_URL_PRE,
    method: 'get',
    params: query
  })
}
// 创建群组
export function addJobs(data) {
  return request({
    url: SCHEDULER_URL_PRE,
    method: 'post',
    data: data
  })
}
// 更新群组
export function updateJobs(data) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+data.id,
    method: 'put',
    data: data
  })
}
// 删除群组
export function deleteJobs(id) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+id,
    method: 'delete'
  })
}
// 删除群组
export function getJob(id) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+id,
    method: 'get'
  })
}
export function handleResult(data) {
  return request({
    url: SCHEDULER_URL_PRE+"/exec",
    method: 'post',
    data: data
  })
}