<template>
  <div class="login">
    <div class="row justify-content-center">
      <div class="col-md-6">
        <div class="card">
          <div class="card-header">
            <h3 class="text-center">Login</h3>
          </div>
          <div class="card-body">
            <form @submit.prevent="login">
              <div class="mb-3">
                <label for="username" class="form-label">Username</label>
                <!-- Vulnerable: No input validation, XSS possible -->
                <input 
                  type="text" 
                  class="form-control" 
                  id="username" 
                  v-model="credentials.username" 
                  required
                >
              </div>
              
              <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <input 
                  type="password" 
                  class="form-control" 
                  id="password" 
                  v-model="credentials.password" 
                  required
                >
              </div>
              
              <div class="d-grid">
                <button type="submit" class="btn btn-primary" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  Login
                </button>
              </div>
            </form>
            
            <div class="text-center mt-3">
              <p>Don't have an account? <router-link to="/register">Register here</router-link></p>
            </div>
            
            <!-- Vulnerable: Display error messages without sanitization -->
            <div v-if="error" class="alert alert-danger mt-3" v-html="error"></div>
            <div v-if="success" class="alert alert-success mt-3" v-html="success"></div>
          </div>
        </div>
        
        <!-- Vulnerable: Demo credentials exposed -->
        <div class="card mt-3">
          <div class="card-header">
            <h5>Demo Credentials</h5>
          </div>
          <div class="card-body">
            <p><strong>Member:</strong> john_doe / password123</p>
            <p><strong>Company:</strong> admin / admin123</p>
            <small class="text-muted">These are for testing SQL injection and other vulnerabilities</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      credentials: {
        username: '',
        password: ''
      },
      loading: false,
      error: null,
      success: null
    }
  },
  methods: {
    async login() {
      this.loading = true
      this.error = null
      this.success = null
      
      try {
        console.log('Sending login request...', this.credentials)
        const response = await this.$http.post('/api/auth/login', this.credentials)
        console.log('Login response:', response.data)
        
        if (response.data.success) {
          this.success = 'Login successful! Redirecting...'
          
          // Update parent user state first
          this.$emit('user-logged-in', response.data.user)
          
          // Wait a bit for parent to update, then redirect
          setTimeout(() => {
            this.$router.push('/jobs')
          }, 500)
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        console.error('Login error:', error)
        if (error.response && error.response.data) {
          this.error = error.response.data.message
        } else {
          this.error = 'Login failed. Please try again.'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login {
  margin-top: 2rem;
}

.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
</style>
