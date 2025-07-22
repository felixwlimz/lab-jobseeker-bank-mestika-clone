import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'
import Jobs from '../views/Jobs.vue'
import JobDetail from '../views/JobDetail.vue'
import PostJob from '../views/PostJob.vue'
import MyApplications from '../views/MyApplications.vue'
import Applications from '../views/Applications.vue'
import Applicants from '../views/Applicants.vue'
import TestApplicants from '../views/TestApplicants.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile
  },
  {
    path: '/jobs',
    name: 'Jobs',
    component: Jobs
  },
  {
    path: '/jobs/:id',
    name: 'JobDetail',
    component: JobDetail
  },
  {
    path: '/post-job',
    name: 'PostJob',
    component: PostJob
  },
  {
    path: '/my-applications',
    name: 'MyApplications',
    component: MyApplications
  },
  {
    path: '/applications/:jobId',
    name: 'Applications',
    component: Applications
  },
  {
    path: '/applicants',
    name: 'Applicants',
    component: Applicants
  },
  {
    path: '/test-applicants',
    name: 'TestApplicants',
    component: TestApplicants
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
