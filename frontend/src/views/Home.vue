<template>
  <div class="home">
    <div class="jumbotron bg-light p-5 rounded-3 mb-4">
      <div class="container-fluid py-5">
        <h1 class="display-5 fw-bold">Welcome to JobSearch</h1>
        <p class="col-md-8 fs-4">Find your dream job or hire the best talent. This is a vulnerable application for security training purposes.</p>
        <router-link class="btn btn-primary btn-lg" to="/jobs">Browse Jobs</router-link>
      </div>
    </div>
    
    <div class="row">
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">For Job Seekers</h5>
            <p class="card-text">Create your profile, upload your CV, and apply for jobs.</p>
            <router-link to="/register" class="btn btn-outline-primary">Get Started</router-link>
          </div>
        </div>
      </div>
      
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">For Companies</h5>
            <p class="card-text">Post job openings and find the perfect candidates.</p>
            <router-link to="/register" class="btn btn-outline-primary">Post Jobs</router-link>
          </div>
        </div>
      </div>
      
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Security Training</h5>
            <p class="card-text">This application contains intentional vulnerabilities for educational purposes.</p>
            <a href="#" class="btn btn-outline-danger">Learn More</a>
          </div>
        </div>
      </div>
    </div>
    
    <div class="alert alert-warning mt-4" role="alert">
      <h4 class="alert-heading">⚠️ Security Warning</h4>
      <p>This application is intentionally vulnerable and should only be used for security training purposes. Do not use in production!</p>
      <hr>
      <p class="mb-0">Common vulnerabilities included: SQL Injection, XSS, Broken Access Control, File Upload Issues, and more.</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home'
}
</script>

<style scoped>
.jumbotron {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.card {
  height: 100%;
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-5px);
}
</style>