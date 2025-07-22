<template>
  <div class="register">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card">
          <div class="card-header">
            <h3 class="text-center">Register</h3>
          </div>
          <div class="card-body">
            <form @submit.prevent="register">
              <div class="row">
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="username" class="form-label">Username</label>
                    <!-- Vulnerable: No input validation -->
                    <input 
                      type="text" 
                      class="form-control" 
                      id="username" 
                      v-model="user.username" 
                      required
                    >
                  </div>
                </div>
                
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="email" class="form-label">Email</label>
                    <input 
                      type="email" 
                      class="form-control" 
                      id="email" 
                      v-model="user.email" 
                      required
                    >
                  </div>
                </div>
              </div>
              
              <div class="row">
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="password" class="form-label">Password</label>
                    <!-- Vulnerable: No password strength requirements -->
                    <input 
                      type="password" 
                      class="form-control" 
                      id="password" 
                      v-model="user.password" 
                      required
                    >
                  </div>
                </div>
                
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="role" class="form-label">Role</label>
                    <select class="form-select" id="role" v-model="user.role" required>
                      <option value="">Select Role</option>
                      <option value="member">Job Seeker</option>
                      <option value="company">Company</option>
                    </select>
                  </div>
                </div>
              </div>
              
              <div class="mb-3">
                <label for="fullName" class="form-label">Full Name</label>
                <input 
                  type="text" 
                  class="form-control" 
                  id="fullName" 
                  v-model="user.fullName"
                >
              </div>
              
              <div class="row">
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="phone" class="form-label">Phone</label>
                    <input 
                      type="tel" 
                      class="form-control" 
                      id="phone" 
                      v-model="user.phone"
                    >
                  </div>
                </div>
                
                <div class="col-md-6" v-if="user.role === 'company'">
                  <div class="mb-3">
                    <label for="companyName" class="form-label">Company Name</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      id="companyName" 
                      v-model="user.companyName"
                    >
                  </div>
                </div>
              </div>
              
              <div class="mb-3" v-if="user.role === 'company'">
                <label for="website" class="form-label">Website</label>
                <!-- Vulnerable: No URL validation -->
                <input 
                  type="url" 
                  class="form-control" 
                  id="website" 
                  v-model="user.website"
                  placeholder="https://example.com"
                >
              </div>
              
              <div class="mb-3">
                <label for="address" class="form-label">Address</label>
                <textarea 
                  class="form-control" 
                  id="address" 
                  rows="3" 
                  v-model="user.address"
                ></textarea>
              </div>
              
              <div class="d-grid">
                <button type="submit" class="btn btn-primary" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  Register
                </button>
              </div>
            </form>
            
            <div class="text-center mt-3">
              <p>Already have an account? <router-link to="/login">Login here</router-link></p>
            </div>
            
            <!-- Vulnerable: Display messages without sanitization -->
            <div v-if="error" class="alert alert-danger mt-3" v-html="error"></div>
            <div v-if="success" class="alert alert-success mt-3" v-html="success"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    return {
      user: {
        username: '',
        email: '',
        password: '',
        role: '',
        fullName: '',
        phone: '',
        companyName: '',
        website: '',
        address: ''
      },
      loading: false,
      error: null,
      success: null
    }
  },
  methods: {
    async register() {
      this.loading = true
      this.error = null
      this.success = null
      
      try {
        const response = await this.$http.post('/api/auth/register', this.user)
        
        if (response.data.success) {
          this.success = 'Registration successful! Redirecting to login...'
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        if (error.response && error.response.data) {
          this.error = error.response.data.message
        } else {
          this.error = 'Registration failed. Please try again.'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register {
  margin-top: 2rem;
}

.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
</style>