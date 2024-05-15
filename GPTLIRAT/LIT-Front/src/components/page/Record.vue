<template>
    <div style='height: 95%'>
        <div class='crumbs'>
            <el-breadcrumb separator='/'>
                <el-breadcrumb-item><i class='el-icon-lx-cascades'></i> 测试用例录制</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div style='height: 100%'>
            <el-row :gutter='20' class='el-row'>
                <el-col :span='12' class='el-col'>
                    <div class='grid-content bg-purple' style='text-align: center;padding-top: 5%'>
                        <h2 v-show="screenCap.url === ''" style='text-align: center;line-height: 500px'>
                            请连接您的手机。
                        </h2>
                        <img
                            v-show="screenCap.url !== ''"
                            id='screenCap'
                            :src='screenCap.url'
                            alt='ScreenCap'
                            height='700'
                            @click='imgClicked'
                            style='margin-top: 10%'
                        />
                    </div>
                </el-col>
                <el-col :span='12' class='el-col'>
                    <div class='grid-content bg-purple' style='padding-top: 5%;padding-left: 10px;padding-right: 10px'>
                        <el-steps :active='activeStep' simple style='' finish-status='success'>
                            <el-step title='连接手机'></el-step>
                            <el-step title='测试用例录制'></el-step>
                            <el-step title='命名'></el-step>
                            <el-step title='完成'></el-step>
                        </el-steps>

                        <div style='background-color: #F5F7FA;padding-top: 5%;padding-left: 5%;padding-right: 5%'>
                            <!--第一步-->
                            <div v-if='activeStep===0'>
                                <el-button type='primary' icon='el-icon-refresh' @click='listAllDevices'>刷新设备列表
                                </el-button>
                                <el-button type='success' icon='el-icon-link' @click='connect'
                                           :disabled='linkStatus===1'>连接
                                </el-button>
                                <el-button type='success' icon='el-icon-refresh' @click='reconnect' :disabled='linkStatus===0'>
                                    重新连接
                                </el-button>
                                <el-button type='danger' icon='el-icon-close' @click='disconnect' :disabled='linkStatus===0'>
                                    断开连接
                                </el-button>
                                <h3 style='margin-top: 10px;margin-bottom: 10px'>设备列表</h3>
                                <div class='image-box'>
                                    <el-radio-group v-model='deviceChose' style='display:flex'>
                                        <div v-for='(item, i) in deviceList' style='margin: 10px'>
                                            <el-radio-button :label='item.serialNumber'>
                                                <el-image
                                                    class='image'
                                                    src='https://richardzpc-oss.oss-cn-beijing.aliyuncs.com/%E6%AF%95%E8%AE%BE/%E6%89%8B%E6%9C%BA.png'
                                                    fit='fill'
                                                ></el-image>
                                                <div>
                                                <span>{{
                                                        item.marketingName
                                                    }}</span>
                                                </div>
                                            </el-radio-button>
                                        </div>
                                    </el-radio-group>
                                </div>
                            </div>

                            <!--第二步-->
                            <div v-else-if='activeStep===1'>
                                <el-button type='primary' icon='el-icon-s-promotion' @click='startRecording' key='khjwe' :disabled="record.status">开始脚本录制
                                </el-button>
                                <el-button type='primary' icon='el-icon-folder' @click='endRecording' key='khjweb' :disabled="!record.status || record.busy">完成录制
                                </el-button>
                                <h3 style='margin-top: 10px;margin-bottom: 10px'>脚本录制进度</h3>
                                <el-input v-model="recordText"
                                          id="commandArea"
                                          type="textarea"
                                          :rows="10"
                                          resize="none">>
                                </el-input>
                            </div>
                            <!-- 第三步 -->
                            <div v-else-if='activeStep===2'>
                                <h3 style='margin-top: 10px;margin-bottom: 10px'>用例命名</h3>
                                <el-input v-model="script.name" placeholder="请输入用例名字"></el-input>
                            </div>

                            <!-- 第四步 -->
                            <div v-else-if='activeStep===3' style='text-align: center;height: 500px'>
                                <h1 style='margin-top: 10px;margin-bottom: 10px;line-height: 300px'>恭喜您完成脚本录制!</h1>
                            </div>

                        </div>
                        <div style='background-color: #F5F7FA;padding-left: 5%'>
                            <el-button style='margin-top: 12px;' type='primary' @click='lastStep'
                                       icon='el-icon-caret-left' v-if='activeStep>0&&activeStep<=2'>
                                上一步
                            </el-button>
                            <el-button style='margin-top: 12px;' type='primary' @click='nextStep'
                                       icon='el-icon-caret-right' v-if='activeStep<2'>
                                下一步
                            </el-button>
                            <el-button style='margin-top: 12px;' type='primary' @click='saveName'
                                       icon='el-icon-caret-right' v-if='activeStep===2'>
                                保存
                            </el-button>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<script>
import apis from '@api/RecordAndReplay';

const STATUS_SUCCESS = 2000;
export default {
    name: 'record',
    mounted: function() {
    },
    data: () => ({
        // o未连接 1已连接
        linkStatus: 0,
        deviceChose: null,
        deviceList: [],
        activeStep: 0,
        screenCap: {
            // The websocket to get screenshots.
            ws: undefined,
            // The url to load screenshots used by <img/>.
            url: ''
        },
        dev: {
            // Serial number.
            serialNo: '',
            brand: '',
            name: '',
            // The real resolution of the device.
            resolution: {
                w: 0,
                h: 0
            }
        },
        record: {
            // Whether the recording starts.
            status: false,
            busy: false
        },
        recordText:"",
        script: {
            name:null,
            id: -1,
        },
        // 定时器
        timer:null,
        stepCount:0,
    }),
    computed: {},
    methods: {
        listAllDevices: function() {
            apis.listAllDevices()
                .then(res => {
                    if (res['status'] === STATUS_SUCCESS) {
                        this.deviceList = res['list'];
                        console.log(this.deviceList);
                    } else {
                        console.error('Getting devices failed.');
                    }
                });
        },
        nextStep() {
            this.activeStep++;
        },
        lastStep() {
            this.activeStep--;
        },
        imgClicked: function(event) {
            if (!this.record.status) return;
            if (this.record.busy) return;
            if (this.dev.serialNo === '') return;

            // Calculate the corresponding location of the
            // clicked point in the real phone.
            let img = document.getElementById('screenCap');
            let relativeLocSrc = {
                x: event.clientX - img.getBoundingClientRect().x,
                y: event.clientY - img.getBoundingClientRect().y
            };
            let resolution = this.dev.resolution;
            let src2DestRatio = {
                x: resolution.w / img.getBoundingClientRect().width,
                y: resolution.h / img.getBoundingClientRect().height
            };
            let locDest = {
                x: Math.round(relativeLocSrc.x * src2DestRatio.x),
                y: Math.round(relativeLocSrc.y * src2DestRatio.y)
            };

            this.record.busy = true;
            this.stepCount++;
            this.addMessage("正在处理第"+this.stepCount+"步用例......")
            apis.recordTap({
                    'serialNo': this.dev.serialNo,
                    'appId': "none",
                    'scriptId': this.script.id,
                    'x': locDest.x,
                    'y': locDest.y

            }).then(res => {
                if (res['status'] === STATUS_SUCCESS) {
                    this.script.id = res['id'];
                    this.record.busy = false;
                    this.addMessage("第"+this.stepCount+"步用例处理完成，请进行下一步操作")
                }
            });

        },
        connect: function() {
            if (this.deviceChose == null) {
                this.$message.error('未选择设备');
            }
            let i = 0;
            for (; i < this.deviceList.length; i++) {
                if (this.deviceList[i].serialNumber === this.deviceChose) {
                    this.dev.serialNo = this.deviceList[i]['serialNumber'];
                    this.dev.brand = this.deviceList[i]['brand'];
                    this.dev.name = this.deviceList[i]['marketingName'];
                    this.dev.resolution.w = this.deviceList[i]['resolution'].split('x')[0];
                    this.dev.resolution.h = this.deviceList[i]['resolution'].split('x')[1];
                    break;
                }
            }
            apis.connect(
                {
                    'serialNo': this.dev.serialNo
                }
            ).then(res => {
                if (res['status'] === STATUS_SUCCESS) {
                    console.log(res['data']);
                    this.$message.success('连接成功！');
                    this.linkStatus = 1;
                } else {
                    this.$message.error('连接失败！');
                    console.error('Connection failed.');
                }
            });
            this.connectScreenCapWebSocket(this.dev.serialNo);
        },
        connectScreenCapWebSocket: function(serialNo) {
            this.screenCap.ws = new WebSocket('ws://localhost:1801/ws');
            let ws = this.screenCap.ws;
            ws.onmessage = (msg) => {
                msg.data.arrayBuffer().then(buf => {
                    this.screenCap.url = URL.createObjectURL(new Blob([new Int8Array(buf)]));
                });
            };
            ws.onopen = () => {
                ws.send('device://' + serialNo);
            };
        },
        reconnect: function() {
            apis.reconnect(
                {
                    'serialNo': this.dev.serialNo
                }
            ).then(res => {
                if (res['status'] !== STATUS_SUCCESS) {
                    console.error('Reconnection failed.');
                    this.$message.error('连接失败！');
                } else {
                    this.$message.success('连接成功！');
                }
            });
        },
        disconnect: function() {
            this.screenCap.ws.close()
            apis.disconnect({
                    'serialNo': this.dev.serialNo
                }
            ).then(res => {
                if (res['status'] === STATUS_SUCCESS) {
                    this.screenCap.url = ''
                    this.$message.success('断开成功！');
                    this.linkStatus=0
                } else {
                    console.error('Disconnection failed.')
                    this.$message.error('断开失败！');
                }
            })
        },
        startRecording: function() {
            if (this.dev.serialNo === '') {
                this.$message.error("未选择设备")
            } else {
                this.record.status = true
                this.script.id = -1
            }
            this.addMessage("开始用例录制")
            this.stepCount=0
            // 启动定时器
            this.timer = setInterval(() => {
                //需要做的事情
                this.getMessage()
            }, 500);
        },
        endRecording: function() {
            if (! this.record.status) return
            this.record.status = false
            apis.complete({
                    'scriptId': this.script.id,
                    'serialNo': this.dev.serialNo
                }
            ).then(res => {
                if (res['status'] !== STATUS_SUCCESS) {
                    console.error('Failed to complete recording.')
                    this.$message.error('保存失败！');
                }else{
                    this.$message.success("保存成功");
                    this.addMessage("用例保存成功,共"+this.stepCount+"步操作")
                }
            })
            clearInterval(this.timer)
            this.timer = null
        },
        getMessage(){
            apis.getMessage().then(res=>{
                if(res.success){
                    let messages=res.content
                    let s=""
                    for(let i=0;i<messages.length;i++){
                        s+=messages[i]+"\n"
                    }
                    this.$set(this,"recordText",this.recordText+s)
                    this.focusEnd()
                }else{
                    this.$message.error("获取日志失败")
                }
            })
        },
        focusEnd() {

            let inpEl = document.getElementById("commandArea")
            setTimeout(() => {
                inpEl.scrollTop = inpEl.scrollHeight
            }, 1)
        },
        addMessage(s){
            this.$set(this,"recordText",this.recordText+s+"\n")
        },
        saveName(){
            if(this.script.name==null){
                this.$message.warning("请输入脚本名字")
                return
            }
            if(this.script.id==-1){
                this.$message.warning("未录制脚本")
                return
            }
            apis.updateName({
                scriptId:this.script.id,
                name:this.script.name
            }).then((res) => {
                if (res.success) {
                    this.$message.success('保存成功!');
                    this.activeStep++
                } else {
                    this.$message.error(res.message);
                }
            });
        }
    }
};
</script>
<style>
.image-box {
    background-color: #ffffff;
    height: 500px;
}

.image {
    width: 100px;
    height: 100px;
    background-color: #ffffff
}

.el-row {
    height: 100%;
}

.el-col {
    height: 100%;
}

.bg-purple {
    background: #ffffff;
}

.grid-content {
    border-radius: 4px;
    min-height: 36px;
    height: 95%;
}
</style>
