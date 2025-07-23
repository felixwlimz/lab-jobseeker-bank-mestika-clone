<template>
  <div class="applications">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <!-- Vulnerable: XSS possible -->
        <!-- <h2>Applications for: <span v-html="job ? job.title : 'Loading...'"></span></h2> -->
        <h2>Applications for: {{ job ? job.title : 'Loading...' }}</h2>
        <p class="text-muted" v-if="job">{{ applications.length }} applications received</p>
      </div>
      
      <router-link to="/post-job" class="btn btn-outline-primary">
        Back to My Jobs
      </router-link>
    </div>
    
    <div v-if="loading" class="text-center">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    
    <div v-else>
      <div v-if="applications.length === 0" class="alert alert-info">
        No applications received for this job yet.
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
                <!-- <h5 v-html="application.applicantName"></h5> -->
                <h5>{{ application.applicantName }}</h5>
                <p class="text-muted">
                  Applied on {{ formatDate(application.appliedAt) }} 
                  <!-- <span v-html="application.applicantEmail"></span> -->
                  <span>{{ application.applicantEmail }}</span>
                </p>
                
                <div class="mb-2">
                  <span 
                    class="badge" 
                    :class="getStatusClass(application.status)"
                  >
                    {{ application.status }}
                  </span>
                </div>
                
                <div v-if="application.coverLetter">
                  <h6>Cover Letter:</h6>
                  <!-- Vulnerable: XSS possible -->
                  <!-- <p v-html="application.coverLetter"></p> -->
                  <p>{{application.coverLetter}}</p>
                </div>
                
                <div v-if="application.applicantPhone">
                  <small class="text-muted">
                    <!-- Phone: <span v-html="application.applicantPhone"></span> -->
                    Phone: <span>{{ application.applicantPhone }}</span>
                  </small>
                </div>
              </div>
              
              <div class="col-md-4">
                <div class="d-grid gap-2">
                  <!-- Vulnerable: No authorization check -->
                  <button 
                    class="btn btn-success btn-sm" 
                    @click="updateStatus(application.id)"
                    :disabled="application.status === 'accepted'"
                  >
                    Accept
                  </button>
                  
                  <button 
                    class="btn btn-danger btn-sm" 
                    @click="updateStatus(application.id)"
                    :disabled="application.status === 'rejected'"
                  >
                    Reject
                  </button>
                  
                  <button 
                    class="btn btn-info btn-sm" 
                    @click="updateStatus(application.id)"
                    :disabled="application.status === 'reviewed'"
                  >
                    Mark as Reviewed
                  </button>
                  
                  <!-- Vulnerable: Direct access to user profile -->
                  <button 
                    class="btn btn-outline-primary btn-sm" 
                    @click="viewProfile(application.userId)"
                  >
                    View Profile
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Vulnerable: Display messages without sanitization -->
    <!-- <div v-if="error" class="alert alert-danger mt-3" v-html="error"></div> -->
    <div v-if="error" class="alert alert-danger mt-3">{{ error }}</div>
    <!-- <div v-if="success" class="alert alert-success mt-3" v-html="success"></div> -->
    <div v-if="success" class="alert alert-success mt-3">{{success}}</div>
  </div>
</template>

<script>
export default {
  name: 'Applications',
  data() {
    return {
      job: null,
      applications: [],
      user: null,
      loading: false,
      error: null,
      success: null
    }
  },
  async created() {
    await this.checkAuth()
    await this.loadJob()
    await this.loadApplications()
  },
  methods: {
    async checkAuth() {
      try {
        const response = await this.$http.get('/api/auth/profile')
        if (response.data.success) {
          this.user = response.data.user
          
          if (this.user.role !== 'company') {
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
    
    async loadJob() {
      try {
        const jobId = this.$route.params.jobId
        const response = await this.$http.get(`/api/jobs/${jobId}`)
        
        if (response.data.success) {
          this.job = response.data.job
        }
      } catch (error) {
        this.error = 'Failed to load job details'
      }
    },
    
    async loadApplications() {
      this.loading = true
      try {
        const jobId = this.$route.params.jobId
        // Vulnerable: No access control
        const response = await this.$http.get(`/api/applications/job/${jobId}`)
        
        if (response.data.success) {
          this.applications = response.data.applications
          
          // Load applicant details
          for (let application of this.applications) {
            await this.loadApplicantDetails(application)
          }
        }
      } catch (error) {
        this.error = 'Failed to load applications'
      } finally {
        this.loading = false
      }
    },
    
    async loadApplicantDetails(application) {
      try {
        // Vulnerable: No access control
        const response = await this.$http.get(`/api/user/${application.userId}`)
        if (response.data.success) {
          const user = response.data.user
          application.applicantName = user.fullName || user.username
          application.applicantEmail = user.email
          application.applicantPhone = user.phone
        }
      } catch (error) {
        application.applicantName = 'Unknown'
      }
    },
    
    // async updateStatus(applicationId, status) {
    //   try {
    //     // Vulnerable: No authorization check
    //     const response = await this.$http.put(`/api/applications/${applicationId}`, { status })
        
    //     if (response.data.success) {
    //       this.success = `Application ${status} successfully!`
          
    //       // Update local status
    //       const application = this.applications.find(app => app.id === applicationId)
    //       if (application) {
    //         application.status = status
    //       }
          
    //       setTimeout(() => {
    //         this.success = null
    //       }, 3000)
    //     } else {
    //       this.error = response.data.message
    //     }
    //   } catch (error) {
    //     this.error = 'Failed to update application status'
    //   }
    // },

    async updateStatus(applicationId) {
      try {
        // Vulnerable: No authorization check
        const response = await this.$http.post(`/api/applications/${applicationId}`)
        
        if (response.data.success) {
          // Update local status
          const application = this.applications.find(app => app.id === applicationId)
          if (application) {
            application.status = response.data.status
          }
          
          setTimeout(() => {
            this.success = null
          }, 3000)
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        this.error = 'Failed to update application status'
      }
    },
    
    async viewProfile(userId) {
      try {
        // Vulnerable: Direct access to any user profile
      const response = await this.$http.get(`/api/user/${userId}`)
        
        if (response.data.success) {
          window.open(`/api/user/${userId}`, '_blank')
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        this.error = 'Failed to preview profile'
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
.applications {
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