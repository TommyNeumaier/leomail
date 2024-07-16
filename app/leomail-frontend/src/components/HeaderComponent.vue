<script setup lang="ts">
import {logout} from "@/services/token.service";
</script>

<template>
<div id="headerNav">
  <img alt="LeoMail-Logo" class="logo" src="@/assets/LeoMail.png" width="auto" height="45">
  <div id="HeaderLinkBox">
    <RouterLink to="/projekte" activeClass="highlight" class="HeaderLinks">Projekte</RouterLink>
    <RouterLink to="/personen" activeClass="highlight" class="HeaderLinks">Personen</RouterLink>
  </div>
  <img alt="profil" class="profil-photo" src="@/assets/profil.jpg" width="auto" height="50" @click="logout()">
</div>
</template>

<style scoped>
body{
  margin: 0;
  padding: 0;
}
#headerNav{
  background-color: white;
  display: flex;
  flex-direction: row;
  align-items: center;
  height: 10vh;
  padding-left: 2.5%;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}
.HeaderLinks{
  border-left: black solid 1px;
  border-right: black solid 1px;
  padding: 10%;
  padding-left: 50%;
  padding-right: 50%;
  text-decoration: none;
  color: #1B1B1B;
}
#HeaderLinkBox a{
  font-size: 0.9em;
}
#HeaderLinkBox{
  margin-left: 4%;
  padding-top: 10px;
}
.profil-photo{
  border-radius: 50%;
  margin-left: auto;
  margin-right: 2%;
}

.highlight {
  font-weight: bold;
}
</style>