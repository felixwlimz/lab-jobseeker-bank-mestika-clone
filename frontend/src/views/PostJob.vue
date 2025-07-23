<template>
  <div class="post-job">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <div class="card">
          <div class="card-header">
            <h3>Post a New Job</h3>
          </div>
          <div class="card-body">
            <form @submit.prevent="postJob">
              <!-- Vulnerable: Hidden field manipulation -->
              <input type="hidden" v-model="job.companyId">

              <div class="mb-3">
                <label for="title" class="form-label">Job Title</label>
                <!-- Vulnerable: No input validation -->
                <input type="text" class="form-control" id="title" v-model="job.title" required minlength="3">
              </div>

              <div class="row">
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="location" class="form-label">Location</label>
                    <input type="text" class="form-control" id="location" v-model="job.location">
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="jobType" class="form-label">Job Type</label>
                    <select class="form-select" id="jobType" v-model="job.jobType">
                      <option value="full-time">Full Time</option>
                      <option value="part-time">Part Time</option>
                      <option value="contract">Contract</option>
                      <option value="internship">Internship</option>
                    </select>
                  </div>
                </div>
              </div>

              <div class="row">
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="salaryMin" class="form-label">Minimum Salary (Rp)</label>
                    <input type="number" class="form-control" id="salaryMin" v-model="job.salaryMin" min="0">
                  </div>
                </div>

                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="salaryMax" class="form-label">Maximum Salary (Rp)</label>
                    <input type="number" class="form-control" id="salaryMax" v-model="job.salaryMax">
                  </div>
                </div>
              </div>

              <div class="mb-3">
                <label for="description" class="form-label">Job Description</label>
                <!-- Vulnerable: XSS possible -->
                <textarea class="form-control" id="description" rows="5" v-model="job.description"
                  placeholder="Describe the job role, responsibilities, and what you're looking for..."></textarea>
              </div>

              <div class="mb-3">
                <label for="requirements" class="form-label">Requirements</label>
                <!-- Vulnerable: XSS possible -->
                <textarea class="form-control" id="requirements" rows="4" v-model="job.requirements"
                  placeholder="List the required skills, experience, and qualifications..."></textarea>
              </div>

              <div class="d-grid">
                <button type="submit" class="btn btn-primary" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  Post Job
                </button>
              </div>
            </form>

            <!-- Vulnerable: Display messages without sanitization -->
            <!-- <div v-if="error" class="alert alert-danger mt-3" v-html="error"></div> -->
            <div v-if="error" class="alert alert-danger mt-3">{{ error }}</div>
            <!-- <div v-if="success" class="alert alert-success mt-3" v-html="success"></div> -->
            <div v-if="success" class="alert alert-success mt-3">{{ success }}</div>
          </div>
        </div>

        <!-- My Posted Jobs -->
        <div class="card mt-4">
          <div class="card-header">
            <h4>My Posted Jobs</h4>
          </div>
          <div class="card-body">
            <div v-if="myJobs.length === 0" class="text-center text-muted">
              No jobs posted yet.
            </div>

            <div v-else>
              <div v-for="postedJob in myJobs" :key="postedJob.id" class="card mb-3">
                <div class="card-body">
                  <div class="row">
                    <div class="col-md-8">
                      <!-- Vulnerable: XSS possible -->
                      <!-- <h6 v-html="postedJob.title"></h6> -->
                      <h6>{{ postedJob.title }}</h6>
                      <p class="text-muted small">{{ postedJob.location }} â€¢ {{ postedJob.jobType }}</p>
                      <span class="badge" :class="postedJob.status === 'active' ? 'bg-success' : 'bg-secondary'">
                        {{ postedJob.status }}
                      </span>
                    </div>

                    <div class="col-md-4 text-end">
                      <router-link :to="`/applications/${postedJob.id}`" class="btn btn-sm btn-outline-primary me-2">
                        View Applications
                      </router-link>

                      <!-- Vulnerable: No authorization check -->
                      <!-- <button 
                        class="btn btn-sm btn-outline-danger" 
                        @click="deleteJob(postedJob.id)"
                      >
                        Delete
                      </button> -->
                      <button v-if="canDelete(job)" @click="deleteJob(job.id)">
                        Delete
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PostJob',
  data() {
    return {
      job: {
        companyId: null,
        title: '',
        description: '',
        requirements: '',
        location: '',
        jobType: 'full-time',
        salaryMin: null,
        salaryMax: null
      },
      myJobs: [],
      user: null,
      loading: false,
      error: null,
      success: null
    }
  },
  async created() {
    await this.checkAuth()
    await this.loadMyJobs()
  },
  methods: {
    canDelete(job) {
      return this.currentUser.role === 'admin' || job.companyId === this.currentUser.companyId
    },
    async checkAuth() {
      try {
        const response = await this.$http.get('/api/auth/profile')
        if (response.data.success) {
          this.user = response.data.user

          if (this.user.role !== 'company') {
            this.$router.push('/jobs')
            return
          }

          this.job.companyId = this.user.id
        } else {
          this.$router.push('/login')
        }
      } catch (error) {
        this.$router.push('/login')
      }
    },

    async loadMyJobs() {
      if (!this.user) return

      try {
        // Vulnerable: No access control
        const response = await this.$http.get(`/api/jobs/company/${this.user.id}`)
        if (response.data.success) {
          this.myJobs = response.data.jobs
        }
      } catch (error) {
        console.error('Failed to load jobs:', error)
      }
    },

    async postJob() {
      this.loading = true
      this.error = null
      this.success = null

      try {
        const response = await this.$http.post('/api/jobs', this.job)

        if (response.data.success) {
          this.success = 'Job posted successfully!'

          // Reset form
          this.job = {
            companyId: this.user.id,
            title: '',
            description: '',
            requirements: '',
            location: '',
            jobType: 'full-time',
            salaryMin: null,
            salaryMax: null
          }

          // Reload jobs
          await this.loadMyJobs()
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        if (error.response && error.response.data) {
          this.error = error.response.data.message
        } else {
          this.error = 'Failed to post job. Please try again.'
        }
      } finally {
        this.loading = false
      }
    },

    async deleteJob(jobId) {
      if (!confirm('Are you sure you want to delete this job?')) return

      try {
        // Vulnerable: No authorization check
        const response = await this.$http.delete(`/api/jobs/${jobId}`)

        if (response.data.success) {
          this.success = 'Job deleted successfully!'
          await this.loadMyJobs()
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        this.error = 'Failed to delete job'
      }
    }
  }
}
</script>

<style scoped>
.post-job {
  margin-top: 1rem;
}

.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}
</style>