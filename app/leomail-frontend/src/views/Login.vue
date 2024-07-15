<script setup lang="ts">
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import {KEYCLOAK_CLIENT_SECRET} from "@/configs/keycloak.config";
import HeaderLogin from "@/components/HeaderLoginComponent.vue";

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const router = useRouter();

const handleLogin = async () => {
  try {
    console.log('Starting login process');
    const response = await axios.post('https://kc.tommyneumaier.at/realms/2425-5bhitm/protocol/openid-connect/token', new URLSearchParams({
      client_id: 'leomail',
      client_secret: KEYCLOAK_CLIENT_SECRET,
      grant_type: 'password',
      username: username.value,
      password: password.value,
      scope: "openid"
    }), {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });

    const { access_token, refresh_token } = response.data;

    localStorage.setItem('access_token', access_token);
    localStorage.setItem('refresh_token', refresh_token);
    router.push('/').then(r => {});
  } catch (error) {
    errorMessage.value = 'Invalid username or password';
    console.error('Login failed', error);
  }
};

</script>

<template>
  <header-login></header-login>
<div id="login-container">
  <h2 id="headline">Login</h2>
  <form @submit.prevent="handleLogin">
    <div class="form-group">
      <label for="username" class="login-label">Benutzername:</label>
      <input type="text" id="username" class="formLogin" v-model="username" required />
    </div>
    <div class="form-group">
      <label for="password" class="login-label">Passwort:</label>
      <input type="password" id="password" class="formLogin" v-model="password" required />
    </div>
    <button type="submit" id="loginButton">Login</button>
    <p v-if="errorMessage">{{ errorMessage }}</p>
  </form>
</div>
</template>

<style scoped>
#headline{
  font-weight: bold;
  text-align: center;
}
#login-container{
  width: 35vw;
  height: 55vh;
  padding: 2%;
  margin: auto;
  margin-top: 15vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);;
}

.form-group{
  margin-bottom: 3vh;
}

form{
  margin-top: 3vh;
}

.login-label {
  color: #5A5A5A;
  font-size: 0.8em;
}

#loginButton{
  all: unset;
  width: 30vw;
  border-radius: 5px;
  padding: 0.6vw;
  border: solid 1px #0086D4;
  background-color: #0086D4;
  color: white;
  text-align: center;
  margin-top: 2vh;
}

#loginButton:hover{
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#loginButton:active{
  background-color: #01379a;
}

.formLogin::placeholder {
  color: #B3B3B3;
}

.formLogin {
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.9vw 0.6vw;
  width: 30vw;
  font-size: 0.8em;
}

.formLogin:focus{
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

</style>