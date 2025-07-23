<template>
  <div class="my-applications">
    <h2>My Job Applications</h2>
    
    <div v-if="loading" class="text-center">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    
    <div v-else>
      <div v-if="applications.length === 0" class="alert alert-info">
        You haven't applied for any jobs yet.
        <router-link to="/jobs" class="alert-link">Browse jobs</router-link>
      </div>
      
      <div v-else>
        <div 
          v-for="application in applications" 
          :key="application.id" 
          class="card mb-3"
        >
          <div class="card-body">
            <div class="row">
              <div class="col-md-8">
                <!-- Vulnerable: XSS possible -->
                <!-- <h5 v-html="application.jobTitle"></h5> -->
                <h5>{{ application.jobTitle }}</h5>
                <p class="text-muted">Applied on {{ formatDate(application.appliedAt) }}</p>
                
                <div class="mb-2">
                  <span 
                    class="badge" 
                    :class="getStatusClass(application.status)"
                  >
                    {{ application.status }}
                  </span>
                </div>
                
                <div v-if="application.coverLetter">
                  <small class="text-muted">Cover Letter:</small>
                  <!-- Vulnerable: XSS possible -->
                  <!-- <p class="small" v-html="application.coverLetter"></p> -->
                  <p class="small">{{ application.coverLetter }}</p>
                </div>
              </div>
              
              <div class="col-md-4 text-end">
                <router-link 
                  :to="`/jobs/${application.jobId}`" 
                  class="btn btn-outline-primary"
                >
                  View Job
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Vulnerable: Display messages without sanitization -->
    <!-- <div v-if="error" class="alert alert-danger mt-3" v-html="error"></div> -->
    <div v-if="error" class="alert alert-danger mt-3">{{ error }}</div>
  </div>
</template>

<script>
export default {
  name: 'MyApplications',
  data() {
    return {
      applications: [],
      user: null,
      loading: false,
      error: null
    }
  },
  async created() {
    await this.checkAuth()
    await this.loadApplications()
  },
  methods: {
    async checkAuth() {
      try {
        const response = await this.$http.get('/api/auth/profile')
        if (response.data.success) {
          this.user = response.data.user
          
          if (this.user.role !== 'member') {
            this.$router.push('/jobs')
            return
          }
        } else {
          this.$router.push('/login')
        }
      } catch (error) {
        this.$router.push('/login')
      }
    },
    
    async loadApplications() {
      if (!this.user) return
      
      this.loading = true
      try {
        // Vulnerable: No access control
        const response = await this.$http.get(`/api/applications/user/${this.user.id}`)
        
        if (response.data.success) {
          this.applications = response.data.applications
          
          // Load job details for each application
          for (let application of this.applications) {
            await this.loadJobDetails(application)
          }
        }
      } catch (error) {
        this.error = 'Failed to load applications'
      } finally {
        this.loading = false
      }
    },
    
    async loadJobDetails(application) {
      try {
        const response = await this.$http.get(`/api/jobs/${application.jobId}`)
        if (response.data.success) {
          application.jobTitle = response.data.job.title
          application.jobLocation = response.data.job.location
        }
      } catch (error) {
        application.jobTitle = 'Job not found'
      }
    },
    
    getStatusClass(status) {
      switch (status) {
        case 'pending':
          return 'bg-warning'
        case 'reviewed':
          return 'bg-info'
        case 'accepted':
          return 'bg-success'
        case 'rejected':
          return 'bg-danger'
        default:
          return 'bg-secondary'
      }
    },
    
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('id-ID')
    }
  }
}
</script>

<style scoped>
.my-applications {
  margin-top: 1rem;
}

.card {
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-2px);
}

.badge {
  font-size: 0.9em;
}
</style>