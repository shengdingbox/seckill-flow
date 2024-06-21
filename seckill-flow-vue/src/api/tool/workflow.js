import request from '@/utils/request'

let baseUrl = process.env.BASE_URL

/**
 * 获取角色
 * @param {*} data 
 * @returns 
 */
export function getRoles(data) {
    return request({
        url: `${baseUrl}roles.json`,
        method: 'get',
        params: data
      })
}

/**
 * 获取部门
 * @param {*} data 
 * @returns 
 */
export function getDepartments(data) {
  return request({
    url: `${baseUrl}departments.json`,
    method: 'get',
    params: data
  })
}

/**
 * 获取职员
 * @param {*} data 
 * @returns 
 */
export function getEmployees(data) {
  return request({
    url: `${baseUrl}employees.json`,
    method: 'get',
    params: data
  })
}
/**
 * 获取条件字段
 * @param {*} data 
 * @returns 
 */
export function getConditions(data) {
  return request({
    url: `${baseUrl}conditions.json`,
    method: 'get',
    params: data
  })
}

/**
 * 获取审批数据
 * @param {*} data 
 * @returns 
 */
export function getWorkFlowData(data) {
  return request({
    url: `${baseUrl}data.json`,
    method: 'get',
    params: data
  })
}
/**
 * 设置审批数据
 * @param {*} data 
 * @returns 
 */
export function setWorkFlowData(data) {
  return request({
    url: `${baseUrl}`,
    method: 'post',
    data: data
  })
}