<template>
  <div class="profile">
    <div class="row">
      <div class="col-md-4">
        <div class="card">
          <div class="card-body text-center">
            <!-- Vulnerable: No validation for profile image -->
            <img 
              :src="user.profileImage ? `/uploads/images/${user.profileImage}` : '/default-avatar.png'" 
              class="rounded-circle mb-3" 
              width="150" 
              height="150"
              alt="Profile"
            >
            <h5 v-html="user.fullName || user.username"></h5>
            <p class="text-muted">{{ user.role === 'company' ? 'Company' : 'Job Seeker' }}</p>
            
            <div class="mb-3">
              <label for="profileImage" class="form-label">Upload Profile Image</label>
              <!-- Vulnerable: No file type validation -->
              <input 
                type="file" 
                class="form-control" 
                id="profileImage" 
                @change="uploadProfileImage"
                accept="*/*"
              >
            </div>
            
            <div v-if="user.role === 'member'" class="mb-3">
              <label for="cvFile" class="form-label">Upload CV</label>
              <!-- Vulnerable: No file validation -->
              <input 
                type="file" 
                class="form-control" 
                id="cvFile" 
                @change="uploadCV"
                accept="*/*"
              >
              <small v-if="user.cvFile" class="text-success">Current CV: {{ user.cvFile }}</small>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-md-8">
        <div class="card">
          <div class="card-header">
            <h4>Profile Information</h4>
          </div>
          <div class="card-body">
            <form @submit.prevent="updateProfile">
              <!-- Vulnerable: Hidden field manipulation possible -->
              <input type="hidden" v-model="user.id">
              
              <div class="row">
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="username" class="form-label">Username</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      id="username" 
                      v-model="user.username" 
                      readonly
                    >
                  </div>
                </div>
                
                <div class="col-md-6">
                  <div class="mb-3">
                    <label for="email" class="form-label">Email</label>
                    <!-- Vulnerable: No email validation -->
                    <input 
                      type="email" 
                      class="form-control" 
                      id="email" 
                      v-model="user.email"
                    >
                  </div>
                </div>
              </div>
              
              <div class="mb-3">
                <label for="fullName" class="form-label">Full Name</label>
                <!-- Vulnerable: XSS possible -->
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
                <!-- Vulnerable: No URL validation, XSS possible -->
                <input 
                  type="text" 
                  class="form-control" 
                  id="website" 
                  v-model="user.website"
                >
              </div>
              
              <div class="mb-3" v-if="user.role === 'company'">
                <label for="companyDescription" class="form-label">Company Description</label>
                <!-- Vulnerable: XSS possible -->
                <textarea 
                  class="form-control" 
                  id="companyDescription" 
                  rows="4" 
                  v-model="user.companyDescription"
                ></textarea>
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
                  Update Profile
                </button>
              </div>
            </form>
            
            <!-- Vulnerable: Display messages without sanitization -->
            <div v-if="error" class="alert alert-danger mt-3" v-html="error"></div>
            <div v-if="success" class="alert alert-success mt-3" v-html="success"></div>
          </div>
        </div>
        
        <!-- Skills section for members -->
        <div v-if="user.role === 'member'" class="card mt-3">
          <div class="card-header">
            <h5>Skills</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-8">
                <input 
                  type="text" 
                  class="form-control" 
                  placeholder="Add a skill" 
                  v-model="newSkill.skillName"
                >
              </div>
              <div class="col-md-4">
                <select class="form-select" v-model="newSkill.level">
                  <option value="beginner">Beginner</option>
                  <option value="intermediate">Intermediate</option>
                  <option value="advanced">Advanced</option>
                  <option value="expert">Expert</option>
                </select>
              </div>
            </div>
            <button class="btn btn-sm btn-primary mt-2" @click="addSkill">Add Skill</button>
            
            <div class="mt-3">
              <span 
                v-for="skill in skills" 
                :key="skill.id" 
                class="badge bg-secondary me-2 mb-2"
              >
                {{ skill.skillName }} ({{ skill.level }})
                <button 
                  type="button" 
                  class="btn-close btn-close-white ms-2" 
                  @click="deleteSkill(skill.id)"
                ></button>
              </span>
            </div>
          </div>
        </div>
        
        <!-- Education section for members -->
        <div v-if="user.role === 'member'" class="card mt-3">
          <div class="card-header">
            <h5>Education</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="addEducation" class="mb-3">
              <div class="row">
                <div class="col-md-6">
                  <input 
                    type="text" 
                    class="form-control mb-2" 
                    placeholder="Institution" 
                    v-model="newEducation.institution"
                    required
                  >
                </div>
                <div class="col-md-6">
                  <input 
                    type="text" 
                    class="form-control mb-2" 
                    placeholder="Degree" 
                    v-model="newEducation.degree"
                  >
                </div>
              </div>
              <div class="row">
                <div class="col-md-6">
                  <input 
                    type="text" 
                    class="form-control mb-2" 
                    placeholder="Field of Study" 
                    v-model="newEducation.fieldOfStudy"
                  >
                </div>
                <div class="col-md-3">
                  <input 
                    type="date" 
                    class="form-control mb-2" 
                    placeholder="Start Date" 
                    v-model="newEducation.startDate"
                  >
                </div>
                <div class="col-md-3">
                  <input 
                    type="date" 
                    class="form-control mb-2" 
                    placeholder="End Date" 
                    v-model="newEducation.endDate"
                  >
                </div>
              </div>
              <textarea 
                class="form-control mb-2" 
                rows="2" 
                placeholder="Description" 
                v-model="newEducation.description"
              ></textarea>
              <button type="submit" class="btn btn-sm btn-primary">Add Education</button>
            </form>
            
            <div v-for="edu in education" :key="edu.id" class="card mb-2">
              <div class="card-body p-3">
                <div class="d-flex justify-content-between">
                  <div>
                    <h6 class="mb-1" v-html="edu.institution"></h6>
                    <p class="mb-1 text-muted" v-html="edu.degree + (edu.fieldOfStudy ? ' - ' + edu.fieldOfStudy : '')"></p>
                    <small class="text-muted">
                      {{ formatDate(edu.startDate) }} - {{ edu.endDate ? formatDate(edu.endDate) : 'Present' }}
                    </small>
                    <p class="mt-2 mb-0" v-if="edu.description" v-html="edu.description"></p>
                  </div>
                  <button 
                    class="btn btn-sm btn-outline-danger" 
                    @click="deleteEducation(edu.id)"
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Experience section for members -->
        <div v-if="user.role === 'member'" class="card mt-3">
          <div class="card-header">
            <h5>Experience</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="addExperience" class="mb-3">
              <div class="row">
                <div class="col-md-6">
                  <input 
                    type="text" 
                    class="form-control mb-2" 
                    placeholder="Company" 
                    v-model="newExperience.company"
                    required
                  >
                </div>
                <div class="col-md-6">
                  <input 
                    type="text" 
                    class="form-control mb-2" 
                    placeholder="Position" 
                    v-model="newExperience.position"
                    required
                  >
                </div>
              </div>
              <div class="row">
                <div class="col-md-6">
                  <input 
                    type="date" 
                    class="form-control mb-2" 
                    placeholder="Start Date" 
                    v-model="newExperience.startDate"
                  >
                </div>
                <div class="col-md-6">
                  <input 
                    type="date" 
                    class="form-control mb-2" 
                    placeholder="End Date" 
                    v-model="newExperience.endDate"
                  >
                </div>
              </div>
              <textarea 
                class="form-control mb-2" 
                rows="3" 
                placeholder="Job Description" 
                v-model="newExperience.description"
              ></textarea>
              <button type="submit" class="btn btn-sm btn-primary">Add Experience</button>
            </form>
            
            <div v-for="exp in experience" :key="exp.id" class="card mb-2">
              <div class="card-body p-3">
                <div class="d-flex justify-content-between">
                  <div>
                    <h6 class="mb-1" v-html="exp.position"></h6>
                    <p class="mb-1 text-muted" v-html="exp.company"></p>
                    <small class="text-muted">
                      {{ formatDate(exp.startDate) }} - {{ exp.endDate ? formatDate(exp.endDate) : 'Present' }}
                    </small>
                    <p class="mt-2 mb-0" v-if="exp.description" v-html="exp.description"></p>
                  </div>
                  <button 
                    class="btn btn-sm btn-outline-danger" 
                    @click="deleteExperience(exp.id)"
                  >
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
</template>

<script>
export default {
  name: 'Profile',
  data() {
    return {
      user: {},
      skills: [],
      education: [],
      experience: [],
      newSkill: {
        skillName: '',
        level: 'beginner'
      },
      newEducation: {
        institution: '',
        degree: '',
        fieldOfStudy: '',
        startDate: '',
        endDate: '',
        description: ''
      },
      newExperience: {
        company: '',
        position: '',
        startDate: '',
        endDate: '',
        description: ''
      },
      loading: false,
      error: null,
      success: null
    }
  },
  async created() {
    await this.loadProfile()
    if (this.user.role === 'member') {
      await this.loadSkills()
      await this.loadEducation()
      await this.loadExperience()
    }
  },
  methods: {
    async loadProfile() {
      try {
        const response = await this.$http.get('/api/auth/profile')
        if (response.data.success) {
          this.user = response.data.user
        } else {
          this.$router.push('/login')
        }
      } catch (error) {
        this.$router.push('/login')
      }
    },
    
    async loadSkills() {
      try {
        const response = await this.$http.get(`/api/skills/user/${this.user.id}`)
        if (response.data.success) {
          this.skills = response.data.skills
        }
      } catch (error) {
        console.error('Failed to load skills:', error)
      }
    },
    
    async updateProfile() {
      this.loading = true
      this.error = null
      this.success = null
      
      try {
        const response = await this.$http.put('/api/user/profile', this.user)
        
        if (response.data.success) {
          this.success = 'Profile updated successfully!'
          this.user = response.data.user
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        if (error.response && error.response.data) {
          this.error = error.response.data.message
        } else {
          this.error = 'Update failed. Please try again.'
        }
      } finally {
        this.loading = false
      }
    },
    
    async uploadProfileImage(event) {
      const file = event.target.files[0]
      if (!file) return
      
      const formData = new FormData()
      formData.append('file', file)
      formData.append('userId', this.user.id)
      
      try {
        const response = await this.$http.post('/api/user/upload-profile-image', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        if (response.data.success) {
          this.user.profileImage = response.data.fileName
          this.success = 'Profile image uploaded successfully!'
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        this.error = 'Upload failed. Please try again.'
      }
    },
    
    async uploadCV(event) {
      const file = event.target.files[0]
      if (!file) return
      
      const formData = new FormData()
      formData.append('file', file)
      formData.append('userId', this.user.id)
      
      try {
        const response = await this.$http.post('/api/user/upload-cv', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        if (response.data.success) {
          this.user.cvFile = response.data.fileName
          this.success = 'CV uploaded successfully!'
        } else {
          this.error = response.data.message
        }
      } catch (error) {
        this.error = 'Upload failed. Please try again.'
      }
    },
    
    async addSkill() {
      if (!this.newSkill.skillName.trim()) return
      
      try {
        const skillData = {
          ...this.newSkill,
          userId: this.user.id
        }
        
        const response = await this.$http.post('/api/skills', skillData)
        
        if (response.data.success) {
          this.skills.push(response.data.skill)
          this.newSkill.skillName = ''
          this.newSkill.level = 'beginner'
        }
      } catch (error) {
        this.error = 'Failed to add skill'
      }
    },
    
    async deleteSkill(skillId) {
      try {
        await this.$http.delete(`/api/skills/${skillId}`)
        this.skills = this.skills.filter(skill => skill.id !== skillId)
      } catch (error) {
        this.error = 'Failed to delete skill'
      }
    },
    
    async loadEducation() {
      try {
        const response = await this.$http.get(`/api/education/user/${this.user.id}`)
        if (response.data.success) {
          this.education = response.data.education
        }
      } catch (error) {
        console.error('Failed to load education:', error)
      }
    },
    
    async addEducation() {
      if (!this.newEducation.institution.trim()) return
      
      try {
        const educationData = {
          ...this.newEducation,
          userId: this.user.id
        }
        
        const response = await this.$http.post('/api/education', educationData)
        
        if (response.data.success) {
          this.education.push(response.data.education)
          this.newEducation = {
            institution: '',
            degree: '',
            fieldOfStudy: '',
            startDate: '',
            endDate: '',
            description: ''
          }
        }
      } catch (error) {
        this.error = 'Failed to add education'
      }
    },
    
    async deleteEducation(educationId) {
      try {
        await this.$http.delete(`/api/education/${educationId}`)
        this.education = this.education.filter(edu => edu.id !== educationId)
      } catch (error) {
        this.error = 'Failed to delete education'
      }
    },
    
    async loadExperience() {
      try {
        const response = await this.$http.get(`/api/experience/user/${this.user.id}`)
        if (response.data.success) {
          this.experience = response.data.experience
        }
      } catch (error) {
        console.error('Failed to load experience:', error)
      }
    },
    
    async addExperience() {
      if (!this.newExperience.company.trim() || !this.newExperience.position.trim()) return
      
      try {
        const experienceData = {
          ...this.newExperience,
          userId: this.user.id
        }
        
        const response = await this.$http.post('/api/experience', experienceData)
        
        if (response.data.success) {
          this.experience.push(response.data.experience)
          this.newExperience = {
            company: '',
            position: '',
            startDate: '',
            endDate: '',
            description: ''
          }
        }
      } catch (error) {
        this.error = 'Failed to add experience'
      }
    },
    
    async deleteExperience(experienceId) {
      try {
        await this.$http.delete(`/api/experience/${experienceId}`)
        this.experience = this.experience.filter(exp => exp.id !== experienceId)
      } catch (error) {
        this.error = 'Failed to delete experience'
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      return new Date(dateString).toLocaleDateString('id-ID')
    }
  }
}
</script>

<style scoped>
.profile {
  margin-top: 1rem;
}

.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.badge {
  font-size: 0.9em;
}
</style>
