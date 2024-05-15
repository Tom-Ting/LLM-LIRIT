import Vue from 'vue';
import Router from 'vue-router';

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/',
            redirect: '/record'
        },
        {
            path: '/',
            component: () => import(/* webpackChunkName: "home" */ '../components/common/Home.vue'),
            meta: { title: '自述文件' },
            children: [
                {
                    path: '/404',
                    component: () => import(/* webpackChunkName: "404" */ '../components/page/404.vue'),
                    meta: { title: '404' }
                },
                {
                    path: '/403',
                    component: () => import(/* webpackChunkName: "403" */ '../components/page/403.vue'),
                    meta: { title: '403' }
                },
                {
                    path: '/record',
                    component: () => import( '../components/page/Record.vue'),
                    meta: { title: '用例录制' }
                },
                {
                    path: '/replay',
                    component: () => import( '../components/page/Replay.vue'),
                    meta: { title: '用例回放' }
                },
                {
                    path: '/TestCaseManagement',
                    component: () => import( '../components/page/TestCaseManagement.vue'),
                    meta: { title: '测试用例管理' }
                }
                
            
            ]
            },
            {
                path: '/login',
                component: () => import(/* webpackChunkName: "login" */ '@components/page/Login.vue'),
                meta: { title: '登录' }
            },
        {
            path: '*',
            redirect: '/404'
        }
    ]
});
