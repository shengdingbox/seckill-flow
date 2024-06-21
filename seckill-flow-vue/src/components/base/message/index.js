import { Message } from 'element-ui'
import './message.scss'

const defaultOpts = {
  message: '', // 内容
  type: 'success', // 类型
  customClass: 'ui-message',
  duration: 3000, // 显示时间
  showClose: false, // 是否显示关闭按钮
  center: false // 文字是否居中
}

export const message = options => {
  let _opts = {}
  if (typeof options === 'string') {
    _opts = JSON.parse(JSON.stringify(defaultOpts))
    _opts.message = options
  } else {
    _opts = Object.assign({}, defaultOpts, options)
  }

  return Message(_opts)
}
