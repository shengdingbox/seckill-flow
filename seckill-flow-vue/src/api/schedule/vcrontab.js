// vcrontab库依赖element-ui
// 因此在这里全局引入element-ui
// 但是全局引入后，messsage、alert、comfirm又会变成element-ui的默认样式
// 所以这里再次引入并覆盖默认的方法
import Vue from 'vue'
import ElementUI from 'element-ui'
import { message } from '@/components/base/message'
import { confirm, alert } from '@/components/base/messageBox'

Vue.use(ElementUI)
Vue.prototype.$message = message
Vue.prototype.$confirm = confirm
Vue.prototype.$alert = alert
