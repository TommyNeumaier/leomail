<script setup lang="ts">
import {logout} from "@/services/auth.service";
</script>

<template>
  <div id="headerNav">
    <RouterLink to="/"><img alt="LeoMail-Logo" class="logo" src="@/assets/LeoMail.png" width="auto" height="45"></RouterLink>
    <div id="HeaderLinkBox">
      <RouterLink to="/" activeClass="highlight" class="HeaderLinks">Projekte</RouterLink>
      <RouterLink to="/personen" activeClass="highlight" class="HeaderLinks">Personen</RouterLink>
      <RouterLink to="/authtest" activeClass="highlight" class="HeaderLinks">JWT</RouterLink>
    </div>
    <div id="profilBox">
      <img alt="profil" class="profil-photo" src="@/assets/profil.jpg" width="auto" height="50" @click="logout()">
    </div>
  </div>
</template>

<style scoped>
#profilBox{
  margin-left: auto;
  margin-right: 2%;
}

body {
  margin: 0;
  padding: 0;
}

#headerNav {
  background-color: white;
  display: flex;
  flex-direction: row;
  align-items: center;
  height: 10vh;
  padding-left: 2.5%;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

.HeaderLinks {
  border-left: black solid 1px;
  border-right: black solid 1px;
  padding: 10%;
  padding-left: 50%;
  padding-right: 50%;
  text-decoration: none;
  color: #1B1B1B;
}

#HeaderLinkBox a {
  font-size: 0.9em;
}

#HeaderLinkBox {
  margin-left: 4%;
  padding-top: 10px;
}
.highlight {
  font-weight: bold;
}
</style>