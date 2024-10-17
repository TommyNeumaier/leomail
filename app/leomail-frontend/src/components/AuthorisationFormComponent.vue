<script setup lang="ts">
import {onMounted, ref} from "vue";
import {Service} from "@/services/service";
import router from "@/configs/router.config";


const profileData = ref();
const email = ref();
const password = ref();
const displayName = ref();
const errorMessage = ref();


onMounted(() => {
  getProfile();
});

const getProfile = async () => {
  const response = await Service.getInstance().getProfile();
  profileData.value = response.data;
  email.value = profileData.value.mailAddress;
};

const handleAuth = () => {
  console.log('Authorisation completed');
  console.log(router.currentRoute)
};
</script>

<template>
  <form @submit.prevent="handleAuth">
    <div class="form-group">
      <label for="username" class="auth-label">Anzeigename:</label><br>
      <input type="text" id="username" class="formAuth" v-model="displayName" required />
    </div>
    <div class="form-group">
      <label for="username" class="auth-label">Email-Adresse:</label><br>
      <input type="text" id="username" class="formAuth" v-model="email" disabled />
    </div>
    <div class="form-group">
      <label for="password" class="auth-label">Passwort:</label><br>
      <input type="password" id="password" class="formAuth" v-model="password" required />
    </div>
    <button type="submit" id="submitButton">Weiter</button>
    <p v-if="errorMessage">{{ errorMessage }}</p>
  </form>
</template>

<style scoped>
.form-group {
  margin-bottom: 3vh;
}

form {
  margin-top: 3vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-label {
  color: #5a5a5a;
  font-size: 0.8em;
}

#submitButton {
  all: unset;
  width: 30vw;
  border-radius: 5px;
  padding: 0.6vw;
  border: solid 1px #0086d4;
  background-color: #0086d4;
  color: white;
  text-align: center;
  margin-top: 2vh;
}

#submitButton:hover {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#submitButton:active {
  background-color: #01379a;
}

.formAuth::placeholder {
  color: #b3b3b3;
}

.formAuth {
  all: unset;
  border: solid 1px #bebebe;
  border-radius: 5px;
  padding: 0.9vw 0.6vw;
  width: 30vw;
  font-size: 0.8em;
}

.formAuth:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}
</style>