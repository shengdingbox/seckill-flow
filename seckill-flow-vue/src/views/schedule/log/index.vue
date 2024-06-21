<template>
  <div class="app-container">
      <!--用户数据-->
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="调度ID" prop="jobId">
            <el-input
              v-model="queryParams.jobId"
              placeholder="请输入调度ID"
              clearable
              style="width: 240px"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
          <el-form-item label="作业名称" prop="jobName">
            <el-input
              v-model="queryParams.jobName"
              placeholder="请输入作业名称"
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

        <el-table v-loading="loading" :data="logList" >
          <el-table-column label="ID" align="center" key="id" prop="id"  width="50"/>
          <el-table-column label="调度编号" align="center" key="jobId" prop="jobId" />
          <el-table-column label="作业名称" align="center" key="jobName" prop="jobName"  width="200"/>
          <el-table-column label="状态" align="center" key="status" prop="status"  width="100" />
          <el-table-column label="开始时间" align="center" prop="startTime"  width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.startTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="结束时间" align="center" prop="endTime"  width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.endTime) }}</span>
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
                size="mini"
                type="text"
                icon="el-icon-edit"
                @click="handleGet(scope.row)"
                v-hasPermi="['system:user:edit']"
              >查看</el-button>
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
      


    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form"  label-width="80px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="作业名称" prop="jobName">
              <el-input v-model="form.jobName"  :disabled="true"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="状态" prop="status">
              <el-input v-model="form.status"   :disabled="true"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="调度id" prop="jobId">
              <el-input v-model="form.jobId"  :disabled="true"/>
            </el-form-item>
          </el-col>
        </el-row>
      <el-row>
          <el-col :span="24">
            <el-form-item label="开始时间" prop="startTime">
              <el-input v-model="form.startTime"   :disabled="true" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="结束时间" prop="endTime">
              <el-input v-model="form.endTime"   :disabled="true"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="请求体" prop="requestBody">
              <el-input v-model="form.requestBody" type="textarea"  :disabled="true"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="响应体" prop="responseBody">
              <el-input v-model="form.responseBody" type="textarea" :disabled="true" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { queryAllLogs,getLog} from "@/api/schedule/log";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "Group",
  dicts: ['sys_normal_disable', 'sys_user_sex'],
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
      logList: null,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
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
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        jobName: undefined,
        jobId: undefined
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
      queryAllLogs(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.logList = response.data.rows;
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
    /** 修改按钮操作 */
    handleGet(row) {
      this.reset();
      const id = row.id || this.ids;
      getLog(id).then(response => {
          this.form = response.data;
          this.loading = false;
        }
      )
        this.open = true;
        this.title = "查看日志";
    }
  }
};
</script>