import request from '@/utils/request'

const SCHEDULER_URL_PRE = '/v1/logs' // 调度器公共路径

// 查询所有日志
export function queryAllLogs(query) {
  return request({
    url: SCHEDULER_URL_PRE,
    method: 'get',
    params: query
  })
}

// 获取日志
export function getLog(id) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+id,
    method: 'get'
  })
}