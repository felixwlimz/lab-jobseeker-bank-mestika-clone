<template>
  <div class="jobs">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>Job Listings</h2>
      <div class="d-flex gap-2">
        <!-- Vulnerable: XSS possible in search -->
        <input 
          type="text" 
          class="form-control" 
          placeholder="Search jobs..." 
          v-model="searchKeyword"
          style="width: 300px;"
        >
        <button class="btn btn-outline-primary" @click="searchJobs">Search</button>
      </div>
    </div>
    
    <div class="row">
      <div class="col-md-12">
        <div v-if="loading" class="text-center">
          <div class="spinner-border" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>
        
        <div v-else>
          <div v-if="jobs.length === 0" class="alert alert-info">
            No jobs found.
          </div>
          
          <div v-else>
            <div v-if="!searchKeyword" class="alert alert-info">
              Showing all jobs ({{ jobs.length }} jobs found).
            </div>
            <div v-else class="alert alert-info">
              Showing results for: <strong>{{ searchKeyword }}</strong> ({{ jobs.length }} jobs found)
            </div>
            
            <div 
              v-for="job in jobs" 
              :key="job.id" 
              class="card mb-3"
            >
              <div class="card-body">
                <div class="row">
                  <div class="col-md-8">
                    <h5 class="card-title">{{ job.title }}</h5>
                    <p class="card-text text-muted">{{ job.location }}</p>
                    <p class="card-text">{{ job.description }}</p>
                    
                    <div class="d-flex gap-2 mb-2">
                      <span class="badge bg-primary">{{ job.jobType }}</span>
                      <span class="badge bg-success" v-if="job.salaryMin">
                        Rp {{ formatCurrency(job.salaryMin) }} - Rp {{ formatCurrency(job.salaryMax) }}
                      </span>
                    </div>
                  </div>
                  
                  <div class="col-md-4 text-end">
                    <router-link 
                      :to="`/jobs/${job.id}`" 
                      class="btn btn-primary"
                    >
                      View Details
                    </router-link>
                    
                    <button 
                      v-if="user && user.role === 'member'" 
                      class="btn btn-success ms-2" 
                      @click="quickApply(job.id)"
                    >
                      Quick Apply
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Vulnerable: Display messages without sanitization -->
    <div v-if="error" class="alert alert-danger mt-3" v-html="error"></div>
    <div v-if="success" class="alert alert-success mt-3" v-html="success"></div>
  </div>
</template>

<script>
export default {
  name: 'Jobs',
  data() {
    return {
      jobs: [],
      searchKeyword: '',
      loading: false,
      error: null,
      success: null,
      user: null
    }
  },
  async created() {
    await this.loadJobs()
    await this.checkAuth()
  },
  methods: {
    async loadJobs() {
      this.loading = true
      console.log('Starting loadJobs...')
      try {
        const response = await this.$http.get('/api/jobs')
        console.log('API Response:', response.data)
        console.log('Jobs array:', response.data.jobs)
        console.log('Jobs length:', response.data.jobs ? response.data.jobs.length : 'undefined')
        
        if (response.data.success) {
          this.jobs = response.data.jobs
          console.log('Jobs set to:', this.jobs)
        }
      } catch (error) {
        console.error('Error loading jobs:', error)
        this.error = 'Failed to load jobs'
      } finally {
        this.loading = false
        console.log('Loading finished. Final jobs:', this.jobs)
      }
    },
    
    async searchJobs() {
      if (!this.searchKeyword.trim()) {
        await this.loadJobs()
        return
      }
      
      this.loading = true
      try {
        // Vulnerable: Direct use of search parameter without sanitization
        const response = await this.$http.get(`/api/jobs/search?keyword=${this.searchKeyword}`)
        if (response.data.success) {
          this.jobs = response.data.jobs
          // Keep searchKeyword to display what user searched
        }
      } catch (error) {
        this.error = 'Search failed'
      } finally {
        this.loading = false
      }
    },
    
    async checkAuth() {
      try {
        const response = await this.$http.get('/api/auth/profile')
        if (response.data.success) {
          this.user = response.data.user
        }
      } catch (error) {
        // Not authenticated
      }
    },
    
    async quickApply(jobId) {
      if (!this.user) {
        this.$router.push('/login')
        return
      }
      
      try {
        const applicationData = {
          jobId: jobId,
          userId: this.user.id,
          coverLetter: 'Quick application submitted'
        }
        
        const response = await this.$http.post('/api/applications', applicationData)
        
        if (response.data.success) {
          this.success = 'Application submitted successfully!'
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        this.error = 'Application failed'
      }
    },
    
    formatCurrency(amount) {
      return new Intl.NumberFormat('id-ID').format(amount)
    }
  }
}
</script>

<style scoped>
.jobs {
  margin-top: 1rem;
}

.card {
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-2px);
}

.badge {
  font-size: 0.8em;
}
</style>
