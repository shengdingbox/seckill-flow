<template>
  <div class="app-container">
    <!--用户数据-->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="群组名称" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入群组名称" clearable style="width: 240px"
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAddShell"
          v-hasPermi="['system:user:add']">新增任务</el-button>
      </el-col>
      <el-col :span="1.5">
            <el-button
              type="info"
              plain
              icon="el-icon-upload2"
              size="mini"
              @click="handleImport"
              v-hasPermi="['system:user:import']"
            >导入</el-button>
          </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="jobList">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="作业名称" align="center" key="name" prop="name" />
      <el-table-column label="群组名称" align="center" key="groupName" prop="groupName" />
      <el-table-column label="作业类型" align="center" key="type" prop="type">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.job_type" :value="scope.row.type" />
        </template> >
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template slot-scope="scope" v-if="scope.row.userId !== 1">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['system:user:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['system:user:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />



    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1000px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="作业名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入作业名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="群组" prop="groupId">
              <el-select v-model="form.groupId" placeholder="请选择群组">
                <el-option v-for="item in groupOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务类型" prop="type" size="medium">
              <el-select v-model="form.type" placeholder="请选择作业类型">
                <el-option v-for="item in dict.type.job_type" :key="item.value" :label="item.label" :value="item.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="请求地址" prop="httpUrl" v-if="form.type == 1">
              <el-input v-model="form.httpUrl" placeholder="请输入请求地址"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="请求方法" prop="httpMethod" v-if="form.type == 1">
              <el-select v-model="form.httpMethod" placeholder="请选择请求方法">
                <el-option v-for="item in methodOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.httpMethod !== 'GET'">
            <el-form-item label="Content-type" width="200px" prop="httpContextType" v-if="form.type == 1">
              <el-select v-model="form.httpContextType" placeholder="请选择Content-type">
                <el-option v-for="item in contentOptions" :key="item.id" :label="item.name"
                  :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="请求头" prop="httpHeader" v-if="form.type == 1">
              <el-input v-model="form.httpHeader" type="textarea" placeholder="请输入请求头" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" v-if="form.httpMethod !== 'GET'">
            <el-form-item label="请求体" prop="httpBody" v-if="form.type == 1">
              <el-input v-model="form.httpBody" type="textarea" placeholder="请输入请求体" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="数据源" prop="datasource" v-if="form.type == 3">
              <el-select v-model="form.datasource" placeholder="请选择数据源">
                <el-option v-for="item in dataSourceOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="表达式" prop="scriptCommand" v-if="form.type != 1">
          <codeEditor  ref="codeEditorRef" :valueCodeLang="getCodeLang(this.form.type)" v-model="form.scriptCommand"  :valueContent="form.scriptCommand" @change="editorChange"></codeEditor>
        </el-form-item>
    </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="提取字段" prop="getField">
              <el-input v-model="form.getField" placeholder="请输入请求地址">
                <el-button slot="append" type="primary" icon="el-icon-video-play" @click="handleResult"></el-button>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
    <!-- cron配置 -->
    <el-dialog :visible="dialogSwitchtree" :title="'cron配置'" :width="'60%'" :top="'1vh'">
      <el-tree :data="treeData"  show-checkbox node-key="lable"  empty-text="加载中，请稍候" ref="resource" ></el-tree>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitApiResult">确 定</el-button>
        <el-button @click="dialogSwitchtree = false">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
        :limit="1"
        accept=".chlsj, .txt,.json,.xml"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" /> 是否更新已经存在的用户数据
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <!-- <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下载模板</el-link> -->
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
    <!-- cron配置 -->
    <el-dialog :visible="dialogSwitchtree" :title="'cron配置'" :width="'60%'" :top="'1vh'">
      <el-tree :data="treeData"  show-checkbox node-key="lable"  empty-text="加载中，请稍候" ref="resource" ></el-tree>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitApiResult">确 定</el-button>
        <el-button @click="dialogSwitchtree = false">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
        :limit="1"
        accept=".chlsj, .txt,.json,.xml"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" /> 是否更新已经存在的用户数据
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <!-- <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下载模板</el-link> -->
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { queryAllJobs, deleteJobs, addJobs, updateJobs, getJob, handleResult } from "@/api/schedule/list";
import { listDatasource} from "@/api/tool/datasource";
import { queryAllGroups } from "@/api/group/list";
import { getToken } from "@/utils/auth";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import codeEditor from '@/components/codeEditor/';

export default {
  name: "Jobs",
  dicts: ['job_type'],
  components: {codeEditor},
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 群组表格数据
      importJobList:null,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      dialogSwitchtree: false,
      // 默认密码
      initPassword: undefined,
      // 日期范围
      dateRange: [],
      // 岗位选项
      postOptions: [],
      // 角色选项
      groupOptions: [],
      dataSourceOptions:[],
      // 表单参数
      form: {
      },
      defaultProps: {
        children: "children",
        label: "label"
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined
      },
      // 列信息
      columns: [
        { key: 0, label: `用户编号`, visible: true },
        { key: 1, label: `用户名称`, visible: true },
        { key: 2, label: `用户昵称`, visible: true },
        { key: 3, label: `手机号码`, visible: true },
        { key: 4, label: `状态`, visible: true },
        { key: 5, label: `创建时间`, visible: true }
      ],
      // 表单校验
      rules: {
        name: [
          { required: true, message: "作业名称不能为空", trigger: "blur" },
          { min: 2, max: 20, message: '作业名称长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        groupId: [
          { required: true, message: "群组不能为空", trigger: "blur" }
        ],
        type: [
          { required: true, message: "作业类型不能为空", trigger: "blur" }
        ],
        scriptCommand: [
          { required: true, message: "脚本内容不能为空", trigger: "blur" }
        ],

        httpUrl: [
          { required: true, message: "请求地址不能为空", trigger: "blur" }
        ],
        httpMethod: [
          { required: true, message: "请求方法不能为空", trigger: "blur" }
        ],
        httpContextType: [
          { required: true, message: "请求类型不能为空", trigger: "blur" }
        ],
        httpHeader: [
          { required: true, message: "请求头不能为空", trigger: "blur" }
        ],
        httpBody: [
          { required: true, message: "请求体不能为空", trigger: "blur" }
        ],
      },
      methodOptions: [
        { "id": "GET" },
        { "id": "POST" },
        { "id": "DELETE" },
        { "id": "PUT" },
        { "id": "PATCH" }
      ],
      contentOptions: [
        { "id": "application/json" },
        { "id": "application/form-data" }
      ],
      treeData: undefined,
      // 用户导入参数
      upload: {
        // 是否显示弹出层（用户导入）
        open: false,
        // 弹出层标题（用户导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/v1/jobs/importJob"
      },
      tableKey: Date.now() // 强制刷新
    };
  },
  watch: {

  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询用户列表 */
    getList() {
      this.loading = true;
      queryAllJobs(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.jobList = response.data.records;
        this.total = response.data.total;
        this.loading = false;
      }
      );
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        jobChildList:[]
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAddShell() {
      this.reset();
      this.open = true;
      this.title = "添加shell任务";
      queryAllGroups().then(response => {
        this.groupOptions = response.data.records;
       
      }),
      listDatasource().then(response=>{
        this.dataSourceOptions=response.data.rows;
      }
      )
     
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getJob(id).then(response => {
        this.form = response.data;
        this.form.jobChildList=response.data.jobHttpDTOList
        this.open = true;
      });
      this.form.name = row.name;
      this.form.description = row.description;
      this.form.id = row.id;

      this.open = true;
      this.title = "修改群组";
    },
    /** 提交按钮 */
    submitForm: function () {
      console.log(this.form)
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateJobs(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addJobs(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id || this.ids;
      const name = row.name;
      this.$modal.confirm('是否确认删除群组名称为"' + name + '"的数据项？').then(function () {
        return deleteJobs(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
    handleResult() {
      this.form.type=this.form.type;
      const code = this.$refs.codeEditorRef.content;
      console.log(code)
      handleResult(this.form).then(response => {
        //const json = JSON.parse(response.data)
        console.log(response.data);
        this.treeData = response.data;
        this.dialogSwitchtree = true
      })
    },
    /** 提交按钮（分配接口） */
    submitApiResult: function () {
        let checkedKeys = this.$refs.resource.getCheckedNodes(true);
        console.log(checkedKeys)
        let resourceIds1 = checkedKeys.map(node => node.path.split(':')[0]).join(",");
        this.form.getField = resourceIds1
        this.dialogSwitchtree=false;
        console.log(resourceIds1);
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = "用户导入";
      this.upload.open = true;
    },
        // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      console.log(response);
      this.getList();
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 提交上传文件
    submitFileForm() {
    this.$refs.upload.submit();
   },
  editorChange(){
  },
  getCodeLang(targetValue){
    const data = this.dict.type.job_type;
    console.log(JSON.stringify(data))
    for (const item of data) {
    if (item.raw.dictValue === targetValue) {
      return item.raw.dictLabel;
    }
  }
  return "text"; 
  }
}
};
</script>
<style scoped>
.bordered-form {
  border: 1px solid #000; /* 修改颜色和宽度以满足你的设计需求 */
  border-radius: 4px; /* 可选，为边框添加圆角 */
  padding: 10px; /* 可选，为表单内部内容添加一些内边距 */
}
</style>
