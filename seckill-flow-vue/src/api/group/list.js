import request from '@/utils/request'

const SCHEDULER_URL_PRE = '/v1/groups' // 调度器公共路径

// 查询所有群组
export function queryAllGroups(query) {
  return request({
    url: SCHEDULER_URL_PRE,
    method: 'get',
    params: query
  })
}
// 创建群组
export function addGroup(data) {
  return request({
    url: SCHEDULER_URL_PRE,
    method: 'post',
    data: data
  })
}
// 更新群组
export function updateGroup(data) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+data.id,
    method: 'put',
    data: data
  })
}
// 删除群组
export function deleteGroup(id) {
  return request({
    url: SCHEDULER_URL_PRE+"/"+id,
    method: 'delete'
  })
}
