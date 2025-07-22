<template>
  <div class="job-detail" v-if="job">
    <div class="row">
      <div class="col-md-8">
        <div class="card">
          <div class="card-body">
            <!-- Vulnerable: XSS possible in job details -->
            <h2 v-html="job.title"></h2>
            <p class="text-muted">{{ job.location }} • {{ job.jobType }}</p>
            
            <div class="mb-3" v-if="job.salaryMin">
              <strong>Salary:</strong> 
              Rp {{ formatCurrency(job.salaryMin) }} - Rp {{ formatCurrency(job.salaryMax) }}
            </div>
            
            <div class="mb-4">
              <h4>Job Description</h4>
              <!-- Vulnerable: XSS possible -->
              <div v-html="job.description"></div>
            </div>
            
            <div class="mb-4">
              <h4>Requirements</h4>
              <!-- Vulnerable: XSS possible -->
              <div v-html="job.requirements"></div>
            </div>
            
            <div class="mb-3">
              <small class="text-muted">
                Posted on {{ formatDate(job.createdAt) }}
              </small>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h5>Apply for this job</h5>
            
            <div v-if="!user" class="text-center">
              <p>Please login to apply for this job.</p>
              <router-link to="/login" class="btn btn-primary">Login</router-link>
            </div>
            
            <div v-else-if="user.role !== 'member'" class="alert alert-warning">
              Only job seekers can apply for jobs.
            </div>
            
            <div v-else>
              <form @submit.prevent="applyForJob">
                <!-- Vulnerable: Hidden field manipulation -->
                <input type="hidden" v-model="application.jobId">
                <input type="hidden" v-model="application.userId">
                
                <div class="mb-3">
                  <label for="coverLetter" class="form-label">Cover Letter</label>
                  <!-- Vulnerable: XSS possible in cover letter -->
                  <textarea 
                    class="form-control" 
                    id="coverLetter" 
                    rows="5" 
                    v-model="application.coverLetter"
                    placeholder="Tell us why you're perfect for this role..."
                  ></textarea>
                </div>
                
                <div class="d-grid">
                  <button 
                    type="submit" 
                    class="btn btn-success" 
                    :disabled="loading || hasApplied"
                  >
                    <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                    {{ hasApplied ? 'Already Applied' : 'Apply Now' }}
                  </button>
                </div>
              </form>
            </div>
            
            <!-- Vulnerable: Display messages without sanitization -->
            <div v-if="error" class="alert alert-danger mt-3" v-html="error"></div>
            <div v-if="success" class="alert alert-success mt-3" v-html="success"></div>
          </div>
        </div>
        
        <!-- Company info -->
        <div class="card mt-3" v-if="company">
          <div class="card-body">
            <h6>About the Company</h6>
            <!-- Vulnerable: XSS possible -->
            <h5 v-html="company.companyName"></h5>
            <p v-html="company.companyDescription"></p>
            
            <div v-if="company.website">
              <!-- Vulnerable: No URL validation -->
              <a :href="company.website" target="_blank" class="btn btn-outline-primary btn-sm">
                Visit Website
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <div v-else class="text-center">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'JobDetail',
  data() {
    return {
      job: null,
      company: null,
      user: null,
      application: {
        jobId: null,
        userId: null,
        coverLetter: ''
      },
      hasApplied: false,
      loading: false,
      error: null,
      success: null
    }
  },
  async created() {
    await this.loadJob()
    await this.checkAuth()
    if (this.user && this.user.role === 'member') {
      await this.checkIfApplied()
    }
  },
  methods: {
    async loadJob() {
      try {
        const jobId = this.$route.params.id
        const response = await this.$http.get(`/api/jobs/${jobId}`)
        
        if (response.data.success) {
          this.job = response.data.job
          this.application.jobId = this.job.id
          
          // Load company info
          await this.loadCompany(this.job.companyId)
        } else {
          this.error = 'Job not found'
        }
      } catch (error) {
        this.error = 'Failed to load job details'
      }
    },
    
    async loadCompany(companyId) {
      try {
        // Vulnerable: No access control
        const response = await this.$http.get(`/api/user/${companyId}`)
        if (response.data.success) {
          this.company = response.data.user
        }
      } catch (error) {
        console.error('Failed to load company info:', error)
      }
    },
    
    async checkAuth() {
      try {
        const response = await this.$http.get('/api/auth/profile')
        if (response.data.success) {
          this.user = response.data.user
          this.application.userId = this.user.id
        }
      } catch (error) {
        // Not authenticated
      }
    },
    
    async checkIfApplied() {
      try {
        // Vulnerable: Exposes all applications
        const response = await this.$http.get(`/api/applications/user/${this.user.id}`)
        if (response.data.success) {
          this.hasApplied = response.data.applications.some(
            app => app.jobId === parseInt(this.$route.params.id)
          )
        }
      } catch (error) {
        console.error('Failed to check application status:', error)
      }
    },
    
    async applyForJob() {
      this.loading = true
      this.error = null
      this.success = null
      
      try {
        const response = await this.$http.post('/api/applications', this.application)
        
        if (response.data.success) {
          this.success = 'Application submitted successfully!'
          this.hasApplied = true
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        if (error.response && error.response.data) {
          this.error = error.response.data.message
        } else {
          this.error = 'Application failed. Please try again.'
        }
      } finally {
        this.loading = false
      }
    },
    
    formatCurrency(amount) {
      return new Intl.NumberFormat('id-ID').format(amount)
    },
    
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('id-ID')
    }
  }
}
</script>

<style scoped>
.job-detail {
  margin-top: 1rem;
}

.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
</style>