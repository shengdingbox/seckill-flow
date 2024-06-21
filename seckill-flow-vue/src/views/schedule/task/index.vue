<template>
  <div class="app-container">
      <!--用户数据-->
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="群组名称" prop="userName">
            <el-input
              v-model="queryParams.userName"
              placeholder="请输入群组名称"
              clearable
              style="width: 240px"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="el-icon-plus"
              size="mini"
              @click="handleAdd"
              v-hasPermi="['system:user:add']"
            >新增</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" ></right-toolbar>
        </el-row>
        <el-table v-loading="loading" :data="groupList">
          <el-table-column label="调度ID" align="center" key="id" prop="id"  fit="true"/>
          <el-table-column label="作业名称" align="center" key="jobName" prop="jobName" fit="true" />
          <el-table-column prop="triggerType" label="表达式类型" >
        <template slot-scope="scope">
          <dict-tag :options="dict.type.trigger_type" :value="scope.row.triggerType"/>
        </template>
      </el-table-column>
          <el-table-column label="间隔" align="center" key="cron" prop="cron" >
            <template slot-scope="scope">
              {{ scope.row.triggerType==='2'?scope.row.cron:scope.row.period+scope.row.timeunit}}
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" width="100">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="1"
              inactive-value="0"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
          <el-table-column label="更新时间" align="center" prop="updateTime"  width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.updateTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            align="center"
            width="200"
            class-name="small-padding fixed-width"
          >
            <template slot-scope="scope" v-if="scope.row.userId !== 1">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handleUpdate(scope.row)"
                v-hasPermi="['system:user:edit']"
              >修改</el-button>
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDelete(scope.row)"
                v-hasPermi="['system:user:remove']"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />
      


    <!-- 新增/修改 任务调度器配置页面 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="群组名称" prop="job">
              <el-input placeholder="请输入内容" v-model="form.job" class="input-with-select">
                <el-button slot="append" type="primary" icon="el-icon-link" @click="handleJob"></el-button>
         </el-input>
        </el-form-item>
        </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="时间类型" prop="triggerType">
              <el-radio v-model="form.triggerType" label="1" border>间隔</el-radio>
              <el-radio v-model="form.triggerType" label="2" border>cron表达式</el-radio>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="时间间隔" prop="period" v-if="form.triggerType == 1">
              <el-input placeholder="请输入时间间隔" v-model="form.period" class="input-with-select">
            </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="时间单位" prop="timeunit" v-if="form.triggerType == 1">
              <el-radio v-model="form.timeunit" label="S" border>秒</el-radio>
              <el-radio v-model="form.timeunit" label="M" border>分</el-radio>
              <el-radio v-model="form.timeunit" label="H" border>小时</el-radio>
              <el-radio v-model="form.timeunit" label="D" border>天</el-radio>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="cron表达式" prop="cron" v-if="form.triggerType == 2">
              <el-input placeholder="请输入内容" v-model="form.cron" class="input-with-select">
              <el-button slot="append" type="primary" icon="el-icon-link" @click="dialogSwitchcron = true"></el-button>
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
    <el-dialog
      :visible="dialogSwitchcron"
      :title="'cron配置'"
      :width="'60%'"
      :top="'1vh'">
      <vcrontab
        :expression="form.cron"
        @hide="dialogSwitchcron = false"
        @fill="crontabFill"></vcrontab>
    </el-dialog>
    <!-- 选择作业 -->
    <el-dialog :visible.sync="dialogSwitchjob"form :title="'选择作业'" :width="'60%'" :showClose="true" @close="dialogSwitchjob = false">
      <div>
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="作业名称" prop="userName">
            <el-input
              v-model="queryParams.userName"
              placeholder="请输入作业名称"
              clearable
              style="width: 240px"
              @keyup.enter.native="handleJobQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleJobQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="Jobloading" :data="dataList">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="作业名称" align="center" key="name" prop="name" />
          <el-table-column label="群组名称" align="center" key="groupName" prop="groupName" />
          <el-table-column label="作业类型" align="center" key="type" prop="type"  />
          <el-table-column label="创建时间" align="center" prop="createTime"  width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            align="center"
            width="160"
            class-name="small-padding fixed-width"
          >
            <template slot-scope="scope" v-if="scope.row.userId !== 1">
              <el-button
                type="success"
                icon="el-icon-check"
                @click="handleJobDetail(scope.row)"
                v-hasPermi="['system:user:edit']"
                circle
              ></el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="Jobtotal>0"
          :total="Jobtotal"
          :page.sync="queryJobParams.pageNum"
          :limit.sync="queryJobParams.pageSize"
          @pagination="getAllJobs"
        />
        </div>
    </el-dialog>
  </div>
</template>

<script>
import { queryAllSchedulers,deleteSchedulers, addSchedulers, updateSchedulers,enableSchedulers,disableSchedulers,getSchedulers} from "@/api/schedule/task";
import { queryAllJobs} from "@/api/schedule/list";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import '@/api/schedule/vcrontab'
import vcrontab from 'vcrontab'

export default {
  components: {
    [vcrontab.name]: vcrontab
  },
  name: "Schedulers",
  dicts: ['trigger_type'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 遮罩层
      Jobloading: true,
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
      // 总条数
      Jobtotal: 0,
      // 群组表格数据
      groupList: null,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 默认密码
      initPassword: undefined,
      // 日期范围
      dateRange: [],
      // 岗位选项
      postOptions: [],
      // 角色选项
      roleOptions: [],
      // 表单参数
      form: {},
      defaultProps: {
        children: "children",
        label: "label"
      },
      searchVal: '',
      dialogSwitchcron: false, // cron配置弹出框
      dialogSwitchjob: false, // 作业选择弹出框
      editType: '', // 编辑模式
      editId: '', // 修改时的ID
      // formRules, // 表单验证规则
      formData: {
        triggerType: '2',
        timeunit: 'S',
        corn: ''
      }, // 表单数据
      dataList: [], // 作业列表数据
      // jobPageConfig, // 作业页面配置
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined
      },
      queryJobParams:{
        pageNum: 1,
        pageSize: 10,
        name: undefined
      },
      jobForm:{
        input3: undefined
      },
      // 表单校验
      rules: {
        job: [
          { required: true, message: "作业名称不能为空", trigger: "blur" }
        ],
        triggerType: [
          { required: true, message: "时间类型不能为空", trigger: "blur" }
        ],
        cron: [
          { required: true, message: "cron表达式不能为空", trigger: "blur" }
        ],
        timeunit: [
          { required: true, message: "时间单位不能为空", trigger: "blur" }
        ],
        period: [
          { required: true, message: "时间间隔不能为空", trigger: "blur" }
        ]
      }
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
      queryAllSchedulers(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.groupList = response.data.records;
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
        id: undefined,
        name: undefined,
        description: undefined,
        status: "0"
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
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加群组";
    },
     /** 新增按钮操作 */
     handleJob() {
      this.dialogSwitchjob = true;
      this.handleJobQuery();
      //this.title = "添加群组";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getSchedulers(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.form.job=row.jobName;
        this.title = "修改调度";
      });
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != undefined) {
            updateSchedulers(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addSchedulers(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除群组名称为"' + name + '"的数据项？').then(function() {
        return deleteGroup(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
     // 自定义配置cron 和 作业
     onSelfSelect (item, val) {
      console.log(item)
      this['dialogSwitch' + item.key] = true
      if (item.key === 'job') {
        this.$nextTick(() => {
          this.getAllJobs()
        })
      }
    },
    // cron组件确认
    crontabFill (val) {
      this.form.cron= val;
      this.dialogSwitchcron = false
    },
    handleJobDetail(row){
      this.form.job= row.name;
      this.form.jobId= row.id;
      this.dialogSwitchjob = false
    },
    handleJobQuery () {
      this.getAllJobs()
      this.Jobloading=false;
    },
    // 选择作业操作
    handleEdit (params) {
      console.log(params)
      const { type, param } = params
      // 选择中列表中某一行
      if (type === 'cellClick') {
        // console.log(this.$refs.form)
        console.log(param)
        this.$refs.form.setFormValue('jobId', param.row.id)
        this.$refs.form.setFormValue('job', param.row.name)
        this.dialogSwitchjob = false
        return
      }
  },
  // 查询作业列表
  getAllJobs () {
    this.Jobloading = true;
      queryAllJobs(this.addDateRange(this.queryJobParams, this.dateRange)).then(response => {
        this.dataList = response.data.records
        this.Jobtotal = response.data.total;
        this.Jobloading = false;
      })
    },
    // 角色状态修改
    handleStatusChange(row) {
      let text = row.status === "1" ? "启用" : "关闭";
      this.$modal
        .confirm('确认要"' + text + '""' + row.jobName + '"任务吗？')
        .then(function () {
          if(row.status === "1"){
            //启动
           return enableSchedulers(row.id);
          }else{
            //关闭
            return disableSchedulers(row.id);
          }
        })
        .then(() => {
          this.$modal.msgSuccess(text + "成功");
        })
        .catch(function () {
          row.status = row.status === "1" ? "0" : "1";
        });
    },
  }
};
</script>
<style lang="scss" scoped>
.job-select-wrap {
  height: 500px;
}
.filter-input-icon {
  top: 6px;
}
</style>