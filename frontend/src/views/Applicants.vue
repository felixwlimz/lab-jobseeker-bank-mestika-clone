<template>
  <div class="applicants">
    <h2>All Job Applicants</h2>
    
    <div v-if="loading" class="text-center">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    
    <div v-else>
      <div v-if="applications.length === 0" class="alert alert-info">
        No applications received yet.
      </div>
      
      <div v-else>
        <div class="row">
          <div class="col-md-3 mb-3">
            <label class="form-label">Filter by Status:</label>
            <select v-model="statusFilter" class="form-select" @change="filterApplications">
              <option value="">All Status</option>
              <option value="pending">Pending</option>
              <option value="reviewed">Reviewed</option>
              <option value="accepted">Accepted</option>
              <option value="rejected">Rejected</option>
            </select>
          </div>
        </div>
        
        <div 
          v-for="application in filteredApplications" 
          :key="application.id" 
          class="card mb-3"
        >
          <div class="card-body">
            <div class="row">
              <div class="col-md-8">
                <h5>{{ application.applicantName || 'Loading...' }}</h5>
                <p class="text-muted">
                  Job: <strong>{{ application.jobTitle || 'Loading...' }}</strong><br>
                  Applied: {{ formatDate(application.appliedAt) }}<br>
                  Email: {{ application.applicantEmail || 'Loading...' }}
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
                  <small class="text-muted">Cover Letter:</small>
                  <p class="small">{{ application.coverLetter.substring(0, 200) }}...</p>
                </div>
              </div>
              
              <div class="col-md-4">
                <div class="d-grid gap-2">
                  <button 
                    class="btn btn-success btn-sm" 
                    @click="updateStatus(application.id, 'accepted')"
                    :disabled="application.status === 'accepted'"
                  >
                    Accept
                  </button>
                  
                  <button 
                    class="btn btn-danger btn-sm" 
                    @click="updateStatus(application.id, 'rejected')"
                    :disabled="application.status === 'rejected'"
                  >
                    Reject
                  </button>
                  
                  <button 
                    class="btn btn-info btn-sm" 
                    @click="updateStatus(application.id, 'reviewed')"
                    :disabled="application.status === 'reviewed'"
                  >
                    Mark as Reviewed
                  </button>
                  
                  <button 
                    class="btn btn-outline-primary btn-sm" 
                    @click="viewDetails(application)"
                  >
                    View Details
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Success/Error Messages -->
    <div v-if="success" class="alert alert-success mt-3">{{ success }}</div>
    <div v-if="error" class="alert alert-danger mt-3">{{ error }}</div>
    
    <!-- Detail Modal -->
    <div v-if="selectedApplication" class="modal fade show d-block" style="background: rgba(0,0,0,0.5)">
      <div class="modal-dialog modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Application Details</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <div class="row">
              <div class="col-md-4 text-center">
                <!-- Profile Image - XSS vulnerable -->
                <img 
                  :src="selectedApplication.profileImage ? `/uploads/images/${selectedApplication.profileImage}` : '/default-avatar.png'" 
                  class="rounded-circle mb-3" 
                  width="150" 
                  height="150"
                  alt="Profile"
                  @error="$event.target.src='/default-avatar.png'"
                >
                <!-- Name with XSS vulnerability -->
                <!-- <h5 v-html="selectedApplication.applicantName"></h5> -->
                <h5>{{ selectedApplication.applicantName }}</h5>
                <!-- <p class="text-muted" v-html="selectedApplication.applicantEmail"></p> -->
                <p class="text-muted">{{ selectedApplication.applicantEmail }}</p>
                
                <!-- CV Download -->
                <div v-if="selectedApplication.cvFile" class="mt-3">
                  <a :href="`/uploads/cv/${selectedApplication.cvFile}`" target="_blank" class="btn btn-outline-primary btn-sm">
                    <i class="fas fa-download"></i> Download CV
                  </a>
                </div>
              </div>
              
              <div class="col-md-8">
                <div class="row">
                  <div class="col-md-6">
                    <h6>Application Information</h6>
                    <!-- Job title with XSS vulnerability -->
                    <!-- <p><strong>Job:</strong> <span v-html="selectedApplication.jobTitle"></span></p> -->
                    <p><strong>Job:</strong> <span>{{ selectedApplication.jobTitle }}</span></p>
                    <p><strong>Applied:</strong> {{ formatDate(selectedApplication.appliedAt) }}</p>
                    <!-- <p><strong>Phone:</strong> <span v-html="selectedApplication.applicantPhone || 'Not provided'"></span></p> -->
                    <p><strong>Phone:</strong> <span>{{ selectedApplication.applicantPhone || 'Not provided' }}</span></p>
                    <p><strong>Status:</strong> 
                      <span class="badge" :class="getStatusClass(selectedApplication.status)">
                        {{ selectedApplication.status }}
                      </span>
                    </p>
                  </div>
                </div>
                
                <!-- Cover Letter with XSS vulnerability -->
                <div v-if="selectedApplication.coverLetter" class="mt-3">
                  <h6>Cover Letter</h6>
                  <!-- <div class="border p-3 bg-light" v-html="selectedApplication.coverLetter"></div> -->
                  <div class="border p-3 bg-light" >{{selectedApplication.coverLetter}}</div>
                </div>
                
                <!-- Skills Section with XSS vulnerability -->
                <div v-if="selectedApplication.skills && selectedApplication.skills.length > 0" class="mt-3">
                  <h6>Skills</h6>
                  <div class="row">
                    <div v-for="skill in selectedApplication.skills" :key="skill.id" class="col-md-6 mb-2">
                      <div class="card">
                        <div class="card-body p-2">
                          <!-- Skill name with XSS vulnerability -->
                          <!-- <strong v-html="skill.skillName"></strong> -->
                          <strong>{{ skill.skillName }}</strong>
                          <span class="badge bg-secondary ms-2">{{ skill.level }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- Education Section with XSS vulnerability -->
                <div v-if="selectedApplication.education && selectedApplication.education.length > 0" class="mt-3">
                  <h6>Education</h6>
                  <div v-for="edu in selectedApplication.education" :key="edu.id" class="card mb-2">
                    <div class="card-body p-3">
                      <!-- Institution with XSS vulnerability -->
                      <!-- <h6 class="mb-1" v-html="edu.institution"></h6> -->
                      <h6 class="mb-1">{{ edu.institution }}</h6>
                      <!-- Degree and field with XSS vulnerability -->
                      <!-- <p class="mb-1 text-muted" v-html="edu.degree + (edu.fieldOfStudy ? ' - ' + edu.fieldOfStudy : '')"></p> -->
                      <p class="mb-1 text-muted">{{ edu.degree + (edu.fieldOfStudy ? ' - ' + edu.fieldOfStudy : '') }}</p>
                      <small class="text-muted">
                        {{ formatDate(edu.startDate) }} - {{ edu.endDate ? formatDate(edu.endDate) : 'Present' }}
                      </small>
                      <!-- Description with XSS vulnerability -->
                      <!-- <p class="mt-2 mb-0" v-if="edu.description" v-html="edu.description"></p> -->
                      <p class="mt-2 mb-0" v-if="edu.description">{{ edu.description }}</p>
                    </div>
                  </div>
                </div>
                
                <!-- Experience Section with XSS vulnerability -->
                <div v-if="selectedApplication.experience && selectedApplication.experience.length > 0" class="mt-3">
                  <h6>Work Experience</h6>
                  <div v-for="exp in selectedApplication.experience" :key="exp.id" class="card mb-2">
                    <div class="card-body p-3">
                      <!-- Company and position with XSS vulnerability -->
                      <!-- <h6 class="mb-1" v-html="exp.company"></h6> -->
                      <h6 class="mb-1">{{ exp.company }}</h6>
                      <!-- <p class="mb-1 text-muted" v-html="exp.position"></p> -->
                      <p class="mb-1 text-muted">{{exp.company}}</p>
                      <small class="text-muted">
                        {{ formatDate(exp.startDate) }} - {{ exp.endDate ? formatDate(exp.endDate) : 'Present' }}
                      </small>
                      <!-- Description with XSS vulnerability -->
                      <!-- <p class="mt-2 mb-0" v-if="exp.description" v-html="exp.description"></p> -->
                      <p class="mt-2 mb-0" v-if="exp.description">{{ exp.description }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeModal">Close</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Applicants',
  data() {
    return {
      applications: [],
      filteredApplications: [],
      user: null,
      loading: false,
      error: null,
      success: null,
      statusFilter: '',
      selectedApplication: null
    }
  },
  async created() {
    console.log('Applicants component created')
    await this.checkAuth()
    await this.loadAllApplications()
  },
  methods: {
    async checkAuth() {
      try {
        const response = await this.$http.get('/api/auth/profile')
        if (response.data.success) {
          this.user = response.data.user
          if (this.user.role !== 'company') {
            this.$router.push('/jobs')
          }
        } else {
          this.$router.push('/login')
        }
      } catch (error) {
        this.$router.push('/login')
      }
    },
    
    async loadAllApplications() {
      this.loading = true
      try {
        const response = await this.$http.get('/api/applications')
        
        if (response.data.success) {
          this.applications = response.data.applications
          
          // Load additional details for each application
          for (let application of this.applications) {
            await this.loadJobDetails(application)
            await this.loadApplicantDetails(application)
          }
          
          this.filteredApplications = [...this.applications]
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
        }
      } catch (error) {
        application.jobTitle = 'Job not found'
      }
    },
    
    async loadApplicantDetails(application) {
      try {
        const response = await this.$http.get(`/api/user/${application.userId}`)
        if (response.data.success) {
          const user = response.data.user
          application.applicantName = user.fullName || user.username
          application.applicantEmail = user.email
          application.applicantPhone = user.phone
          application.profileImage = user.profileImage
          application.cvFile = user.cvFile
          
          // Load skills, education, and experience
          await this.loadUserSkills(application)
          await this.loadUserEducation(application)
          await this.loadUserExperience(application)
        }
      } catch (error) {
        application.applicantName = 'Unknown'
      }
    },

    async loadUserSkills(application) {
      try {
        const response = await this.$http.get(`/api/skills/user/${application.userId}`)
        if (response.data.success) {
          application.skills = response.data.skills
        }
      } catch (error) {
        application.skills = []
      }
    },

    async loadUserEducation(application) {
      try {
        const response = await this.$http.get(`/api/education/user/${application.userId}`)
        if (response.data.success) {
          application.education = response.data.education
        }
      } catch (error) {
        application.education = []
      }
    },

    async loadUserExperience(application) {
      try {
        const response = await this.$http.get(`/api/experience/user/${application.userId}`)
        if (response.data.success) {
          application.experience = response.data.experience
        }
      } catch (error) {
        application.experience = []
      }
    },
    
    async updateStatus(applicationId, status) {
      try {
        const response = await this.$http.put(`/api/applications/${applicationId}`, { status })
        
        if (response.data.success) {
          this.success = `Application ${status} successfully!`
          
          // Update local status
          const application = this.applications.find(app => app.id === applicationId)
          if (application) {
            application.status = status
          }
          
          this.filterApplications()
          
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

    // async updateStatus(applicationId) {
    //   try {
    //     // Vulnerable: No authorization check
    //     const response = await this.$http.post(`/api/applications/${applicationId}`)
        
    //     if (response.data.success) {
    //       // Update local status
    //       const application = this.applications.find(app => app.id === applicationId)
    //       if (application) {
    //         application.status = response.data.status
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
    
    filterApplications() {
      if (this.statusFilter === '') {
        this.filteredApplications = [...this.applications]
      } else {
        this.filteredApplications = this.applications.filter(
          app => app.status === this.statusFilter
        )
      }
    },
    
    viewDetails(application) {
      this.selectedApplication = application
    },
    
    closeModal() {
      this.selectedApplication = null
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
.applicants {
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

.modal {
  z-index: 1050;
}
</style>
