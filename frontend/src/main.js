import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

// Bootstrap CSS
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Ensure correct backend URL
axios.defaults.baseURL = 'http://localhost:8083'
axios.defaults.withCredentials = true

// Add request interceptor to ensure credentials
axios.interceptors.request.use(config => {
  config.withCredentials = true
  return config
})

const app = createApp(App)

// Add error handler
app.config.errorHandler = (err, instance, info) => {
  console.error('Vue Error:', err)
  console.error('Component:', instance)
  console.error('Info:', info)
}

// Add router error handling
router.onError((error) => {
  console.error('Router Error:', error)
})

// Debug router
router.beforeEach((to, from, next) => {
  console.log('Navigating to:', to.path)
  next()
})

app.config.globalProperties.$http = axios
app.use(router)
app.mount('#app')
