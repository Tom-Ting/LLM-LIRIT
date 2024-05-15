<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 录制回放测试用例管理 </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container" style="padding: 0px">

            <el-table :data="tableData" border class="table" ref="Table" header-cell-class-name="table-header">
                <el-table-column type="index" :index="indexMethod" width="50" align="center" label="序号"></el-table-column>
                <el-table-column prop="scriptId" label="用例ID" align="center"></el-table-column>
                <el-table-column prop="name" label="用例名称" align="center"></el-table-column>
                <el-table-column prop="deviceUdid" label="设备ID" align="center"></el-table-column>
                <el-table-column prop="dirsLocation" label="用例步数" align="center"></el-table-column>
                <el-table-column label="操作" width="180" align="center">
                    <template slot-scope="scope">
                        <el-button type="success" icon="el-icon-edit" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
                        <el-button type="danger" icon="el-icon-delete" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination">
                <el-pagination
                    background
                    layout="total, prev, pager, next"
                    :current-page="query.pageIndex"
                    :page-size="query.pageSize"
                    :total="pageTotal"
                    @current-change="handlePageChange"
                ></el-pagination>
            </div>
        </div>
        <!-- 编辑弹出框 -->
        <el-dialog title="修改订单" :visible.sync="editVisible" width="50%">
            <el-form ref="editForm" :model="editForm" label-width="150px">
                <el-form-item label="用例名称" prop="name">
                    <el-input v-model="editForm.name"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="editVisible = false">取消</el-button>
                <el-button type="primary" @click="saveEdit()">修改</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
import api from '@api/RecordAndReplay';
export default {
    components: {},
    props: {},
    data() {
        return {
            query: {
                pageIndex: 1,
                pageSize: 10
            },
            idex:-1,
            tableData: [],
            pageTotal: 0,
            editVisible: false,
            editForm: {},
            numericalManageTable:{},
            serviceTable:{}
        };
    },
    watch: {},
    computed: {},
    methods: {
        getData() {
            api.getData(this.query).then((res) => {
                if (res.success) {
                    for(let i=0;i<res.content.list.length;i++){
                        res.content.list[i].dirsLocation=res.content.list[i].dirsLocation.split(",").length
                    }
                    this.tableData = res.content.list;
                    this.pageTotal = res.content.pageTotal || 50;
                } else {
                    this.$message.error(res.message);
                }
            });
        },
        handlePageChange(val) {
            this.$set(this.query, 'pageIndex', val);
            this.getData();
        },
        // 删除操作
        handleDelete(index, row) {
            // 二次确认删除
            this.$confirm('确定要删除吗？', '提示', {
                type: 'warning'
            })
                .then(() => {
                    api.deleteScript({scriptId:row.scriptId}).then(res=>{
                        if(res.success){
                            this.$message.success('删除成功');
                        }else{
                            this.$message.error("删除失败");
                        }
                    })
                    this.tableData.splice(index, 1);
                })
                .catch(() => {});
        },
        indexMethod(index) {
            return index+1+(this.query.pageIndex-1)*this.query.pageSize;
        },
        handleEdit(index, row) {
            this.editForm = JSON.parse(JSON.stringify(row)); //深拷贝
            this.idex = index;
            this.editVisible = true;
        },
        saveEdit() {
            api.updateName({
                scriptId: this.editForm.scriptId,
                name:this.editForm.name
            }).then((res) => {
                if (res.success) {
                    this.$message.success('修改成功!');
                    this.editVisible = false;
                    this.$set(this.tableData, this.idex, this.editForm);
                } else {
                    this.$message.error(res.message);
                }
            });
        },
    },
    created() {
        this.getData();
    },
    mounted() {}
};
</script>
<style scoped>
.handle-box {
    margin: 10px;
}

.handle-select {
    width: 120px;
}

.handle-input {
    width: 300px;
    display: inline-block;
}
.table {
    width: 100%;
    font-size: 14px;
}
.red {
    color: #ff0000;
}
.mr10 {
    margin-right: 10px;
}
.table-td-thumb {
    display: block;
    margin: auto;
    width: 40px;
    height: 40px;
}
</style>