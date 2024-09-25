<script setup lang="ts">
import {logout} from "@/services/auth.service";
import {Service} from "@/services/service";
import {onMounted, ref} from "vue";
import {useAppStore} from "@/stores/app.store";
const profileData = ref();
const name = ref();
const projectName = ref();

onMounted(()=>{
  getProjectName()
  getProfile();
});

const getProfile = async () => {
  const response = await Service.getInstance().getProfile();
  profileData.value = response.data;
  name.value = profileData.value.firstName + " " + profileData.value.lastName;
  console.log(name.value);
};

const getProjectName = async () => {
  const response = await Service.getInstance().getProjectName(useAppStore().$state.project);
  projectName.value = response.data;
  console.log(projectName.value);
};
</script>

<template>
  <div id="headerNav">
    <p>{{projectName}}</p>
    <RouterLink to="/"><img alt="LeoMail-Logo" class="logo" src="@/assets/LeoMail.png" width="auto" height="45"></RouterLink>
    <div id="HeaderLinkBox">
      <RouterLink to="/" activeClass="highlight" class="HeaderLinks">Projekte</RouterLink>
      <RouterLink to="/contacts" activeClass="highlight" class="HeaderLinks">Personen</RouterLink>
      <RouterLink to="/authtest" activeClass="highlight" class="HeaderLinks">JWT</RouterLink>
    </div>
    <div id="profilBox">
      <RouterLink to="/profile">{{name}}</RouterLink>
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
  /*border-left: black solid 1px;
  border-right: black solid 1px;*/
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