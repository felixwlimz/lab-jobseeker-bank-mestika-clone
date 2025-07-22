<template>
  <div id="app">
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
      <div class="container">
        <router-link class="navbar-brand" to="/">JobSearch</router-link>
        
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto">
            <li class="nav-item">
              <router-link class="nav-link" to="/jobs">Jobs</router-link>
            </li>
            <li class="nav-item" v-if="user && user.role === 'company'">
              <router-link class="nav-link" to="/post-job">Post Job</router-link>
            </li>
            <li class="nav-item" v-if="user && user.role === 'company'">
              <router-link class="nav-link" to="/applicants">Applicants</router-link>
            </li>
            <li class="nav-item" v-if="user && user.role === 'member'">
              <router-link class="nav-link" to="/my-applications">My Applications</router-link>
            </li>
          </ul>
          
          <ul class="navbar-nav">
            <li class="nav-item" v-if="!user">
              <router-link class="nav-link" to="/login">Login</router-link>
            </li>
            <li class="nav-item" v-if="!user">
              <router-link class="nav-link" to="/register">Register</router-link>
            </li>
            <li class="nav-item dropdown" v-if="user">
              <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                {{ user.fullName || user.username }}
              </a>
              <ul class="dropdown-menu">
                <li><router-link class="dropdown-item" to="/profile">Profile</router-link></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" @click="logout">Logout</a></li>
              </ul>
            </li>
          </ul>
        </div>
      </div>
    </nav>
    
    <main class="container mt-4">
      <router-view @user-logged-in="updateUser" />
    </main>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      user: null
    }
  },
  async created() {
    await this.checkAuth()
  },
  methods: {
    async checkAuth() {
      try {
        const response = await this.$http.get('/api/auth/profile')
        if (response.data.success) {
          this.user = response.data.user
        }
      } catch (error) {
        this.user = null
        console.log('Not authenticated')
      }
    },
    async logout() {
      try {
        await this.$http.post('/api/auth/logout')
        this.user = null
        this.$router.push('/login')
      } catch (error) {
        console.error('Logout failed:', error)
      }
    },
    updateUser(user) {
      console.log('Updating user:', user)
      this.user = user
      // Don't call checkAuth again since we just got user data
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.navbar-brand {
  font-weight: bold;
}

.container {
  max-width: 1200px;
}
</style>
