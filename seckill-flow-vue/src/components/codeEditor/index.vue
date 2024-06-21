<template>
  <div ref="screenFull">
          <div style="display: flex; width: 100%; margin-right: 20px; margin-bottom: 10px;">
              <el-select
                  v-model="valueTheme"
                  :value-key="'valueTheme'"
                  placeholder="选择主题"
                  style="width: 130px; margin-right: 20px;"
                  :filterable=true
                  :clearable=true
                  @change="selectTheme"
                  :popper-append-to-body="false"
              >
                  <el-option
                      v-for="item in listTheme"
                      :key="item"
                      :label="item"
                      :value="item">
                  </el-option>
              </el-select>

              <el-select
                  v-model="valueCodeLang"
                  :value-key="'valueCodeLang'"
                  placeholder="选择语言"
                  style="width: 120px; margin-right: 20px;"
                  :filterable=true
                  :clearable=true
                  @change="selectLang"
                  :popper-append-to-body="false"
              >
                  <el-option
                      v-for="item in listCodeLang"
                      :key="item"
                      :label="item"
                      :value="item">
                  </el-option>
              </el-select>

              <el-select
                  v-model="valueFontSize"
                  :value-key="'valueFontSize'"
                  placeholder="字体大小"
                  style="width: 100px; margin-right: 20px;"
                  :filterable=true
                  :clearable=true
                  @change="selectFontSize"
                  :popper-append-to-body="false"
              >
                  <el-option
                      v-for="item in listFontSize"
                      :key="item"
                      :label="item"
                      :value="item">
                  </el-option>
              </el-select>

              <el-button theme="primary" icon="script-file" class="mr10" @click="copyCode()"> 复制</el-button>
              <el-button v-if="valueCodeLang === 'json'" theme="primary" icon="eye" class="mr10" @click="formatCode()"> 美化</el-button>

              <el-button theme="primary" :icon=" fullScreen ? 'un-full-screen' : 'full-screen' " class="mr10" @click="screen()"> {{ fullScreen ? '退出全屏' : '全屏' }}</el-button>
              
          </div>
          <div style="display: flex; overflow: auto;">
              <editor
                  ref="aceEditor"
                  v-model="content"
                  @init="editorInit"
                  width="100%"
                  style="min-height:300px;"
                  :height="editorHight"
                  :lang="lang"
                  :theme="theme"
                  :options="{
                      enableBasicAutocompletion: true,
                      enableSnippets: true,
                      enableLiveAutocompletion: true,
                      tabSize: 4, fontSize: fontSize,
                      readOnly: readOnly,//设置是否只读
                      showPrintMargin: false //去除编辑器里的竖线
                  }"
              ></editor>
          </div>      
  </div>
</template>

<script>
  export default {
      name: "codeEditor",
      components: {
          editor: require('vue2-ace-editor')
      },
      data(){
          return {
              listTheme: [
                  'clouds',
                  'clouds_midnight',
                  'dracula',
                  'chrome',
                  'chaos',
                  'xcode',
                  'monokai',
                  'ambiance',
                  'dreamweaver',
                  'eclipse',
                  'github',
                  'idle_fingers'
              ],
              listCodeLang: [
                  'json',
                  'yaml',
                  'xml',
                  'java',
                  'text',
                  'javascript',
                  'scheme',
                  'lua',
                  'mysql',
                  'perl',
                  'powershell',
                  'python',
                  'ruby',
                  'sql',
                  'hjson',
                  'ini'
              ],

              listFontSize: [
                  10, 12, 14, 16, 20
              ],
          
              content: '',
              theme: '',
              lang: '',
              fontSize: 12,
              tmpFontSize: 12,

              fullScreen: false,
              unFullEditorHight: 0, // 非全屏状态下的编辑器高度
              appendToBody: true
          }
      },
      props: {
          // 编辑框高度
          editorHight: {
              type: Number,
              default: 300
          },
          // 是否只读
          readOnly: {
              type: Boolean,
              default: false
          },
          // 要展示的代码
          codeData: {
              type: String,
              default: ''
          },
          // 默认的主题
          valueTheme: {
              type: String,
              default: 'clouds',
              // validator: function (value) { // 自定义验证函数
              //     if (this.listTheme.includes(value)) {
              //         return value
              //     } else {
              //         return 'clouds'
              //     }
              // }
          },
          // 默认的语言
          valueCodeLang: {
              type: String,
              default: 'java',
              // validator: function (value) { // 自定义验证函数
              //     if (this.listCodeLang.includes(value)) {
              //         return value
              //     } else {
              //         return 'text'
              //     }
              // }
          },
          // 默认的语言
          valueFontSize: {
              type: Number,
              default: 12
          },
           // 默认的语言
           valueContent: {
              type: String,
              default: undefined
          }
      },
     
      created () {
          // 初始化主题、语言、大小
          this.theme = this.valueTheme
          this.lang = this.valueCodeLang
          this.fontSize = this.tmpFontSize = this.valueFontSize
          this.content = this.valueContent
      },
      mounted () {
          // 初始化编辑器
          this.editorInit()
          this.unFullEditorHight = this.editorHight // 缓存浏览器高度

          // 若传输代码，则展示代码
          if (this.codeData) {
              console.log(this.codeData)
              this.$refs.aceEditor.editor.setValue(this.codeData)
          }

          // ESC按键事件无法监听，监听窗口变化
          window.onresize = () => {
              this.fullScreen = this.checkFull()
          }
      },

      methods: {
          selectTheme (newValue) {
              if (newValue) {
                  this.theme = newValue
              }
          },
          selectLang (newValue) {
              if (newValue) {
                  this.lang = newValue
              }
          },
          selectFontSize (newValue) {
              if (newValue) {
                  this.fontSize = newValue
                  this.tmpFontSize = newValue
              }
          },
          editorInit () { // 初始化
              require('brace/ext/language_tools')
              require('brace/ext/beautify')
              require('brace/ext/error_marker')
              require('brace/ext/searchbox')
              require('brace/ext/split')

              // 循坏加载语言，通过点击按钮切换
              for (let s = 0; s < this.listCodeLang.length; s++) {
                  require('brace/snippets/' + this.listCodeLang[s])
              }
              for (let j = 0; j < this.listCodeLang.length; j++) {
                  require('brace/mode/' + this.listCodeLang[j])
              }

              // 循坏加载主题，通过点击按钮切换
              for (let i = 0; i < this.listTheme.length; i++) {
                  require('brace/theme/' + this.listTheme[i])
              }
          },

          copyCode () {
              const code = this.$refs.aceEditor.editor.getValue()
              let message, theme
              // 复制到剪切板
              if (navigator.clipboard) {
                  navigator.clipboard.writeText(code)
                  
                  message = '复制成功'
                  theme = 'success'
              } else {
                  message = '您的浏览器不支持自动复制，请手动复制'
                  theme = 'error'
              }

              // this.$bkMessage({
              //     message,
              //     theme
              // })
          },

          formatCode () {
              const code = this.$refs.aceEditor.editor.getValue()
              console.log(code)
              if (code !== '') {
                  const string = JSON.stringify(JSON.parse(code), null, 2)
                  this.$refs.aceEditor.editor.setValue(string)
              }
          },
      
          // getValue () {
          // 获取编辑器中的值
          //     console.log('第一个换行符的位置：' + this.$refs.aceEditor.editor.getValue().indexOf('\n'))
          // }

          checkFull () { // 用来监听编辑器大小是否变化
              // 火狐浏览器
              const isFull
                  = document.mozFullScreen
                      || document.fullScreen
                      // 谷歌浏览器及Webkit内核浏览器
                      || document.webkitIsFullScreen
                      || document.webkitRequestFullScreen
                      || document.mozRequestFullScreen
                      || document.msFullscreenEnabled

              console.log(isFull)
              console.log(!!isFull)
              if (!isFull) { // false时 ：非全屏
                  this.editorHight = this.unFullEditorHight
              }

              return !!isFull
          },
          screen () {
              this.valueFontSize = this.fontSize = 12 // 大小变更后初始化文字大小（不定义也会变为12 组件bug）
              const element = this.$refs.screenFull

              if (this.fullScreen) {
                  this.appendToBody = true
                  this.editorHight = this.unFullEditorHight
                  // 关闭全屏
                  if (document.exitFullscreen) {
                      document.exitFullscreen()
                  } else if (document.webkitCancelFullScreen) {
                      document.webkitCancelFullScreen()
                  } else if (document.mozCancelFullScreen) {
                      document.mozCancelFullScreen()
                  } else if (document.msExitFullscreen) {
                      document.msExitFullscreen()
                  }
              } else {
                  this.appendToBody = false
                  // 全屏
                  this.editorHight = window.screen.availHeight - 50
                  if (element.requestFullscreen) {
                      element.requestFullscreen()
                  } else if (element.webkitRequestFullScreen) {
                      element.webkitRequestFullScreen()
                  } else if (element.mozRequestFullScreen) {
                      element.mozRequestFullScreen()
                  } else if (element.msRequestFullscreen) {
                      // IE11
                      element.msRequestFullscreen()
                  }
              }

              this.fullScreen = !this.fullScreen

              // setTimeout(() => {
              //     console.log(this.tmpFontSize)
              //     this.fontSize = this.tmpFontSize
              // }, 200)
          }
          
      }
  }
</script>

<style>

</style>