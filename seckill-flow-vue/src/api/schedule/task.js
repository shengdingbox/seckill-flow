import request from '@/utils/request'

const SCHEDULER_URL_PRE = '/v1/schedulers' // 调度器公共路径

// 查询所有调度
export function queryAllSchedulers(query) {
  return request({
    url: SCHEDULER_URL_PRE,
    method: 'get',
    params: query
  })
}
// 创建调度
export function addSchedulers(data) {
  return request({
    url: SCHEDULER_URL_PRE,
    method: 'post',
    data: data
  })
}
// 获取调度
export function getSchedulers(id) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+id,
    method: 'get'
  })
}
// 更新调度
export function updateSchedulers(data) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+data.id,
    method: 'put',
    data: data
  })
}
// 删除调度
export function deleteSchedulers(id) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+id,
    method: 'delete'
  })
}
// 启动调度
export function enableSchedulers(id) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+id+":enable",
    method: 'post'
  })
}
// 关闭调度
export function disableSchedulers(id) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+id+":disable",
    method: 'post'
  })
}
