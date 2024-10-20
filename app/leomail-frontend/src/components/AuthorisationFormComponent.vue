<script setup lang="ts">
import {onMounted, ref} from "vue";
import {Service} from "@/services/service";
import router from "@/configs/router.config";

const profileData = ref<any | null>(null); // User profile data
const email = ref<string | null>(null); // Outlook email
const password = ref<string>(""); // Outlook password entered by user
const errorMessage = ref<string | null>(null); // Error message for validation

onMounted(async () => {
  try {
    const outlookAuthStatus = await Service.getInstance().checkOutlookAuthorization();
    if (outlookAuthStatus) {
      // Outlook is already authorized; redirect to 'projects'
      router.replace({name: 'projects'});
    } else {
      await getProfile();
    }
  } catch (error) {
    console.error('Fehler beim Überprüfen der Outlook-Autorisierung:', error);
    await getProfile(); // Proceed to getProfile even if Outlook check fails
  }
});

// Fetches the user's email from the backend
const getProfile = async () => {
  try {
    const response = await Service.getInstance().getProfile();
    profileData.value = response.data;
    email.value = profileData.value.mailAddress; // Automatically fill email from the backend
  } catch (error) {
    errorMessage.value = "Fehler beim Abrufen des Profils.";
  }
};

// Handle the authorization and submission of the Outlook password
const handleAuth = async () => {
  try {
    // Send the email and password to the backend to store the encrypted password
    const response = await Service.getInstance().saveOutlookPassword({
      email: email.value?.replace("students.htl-leonding.ac.at", "htblaleonding.onmicrosoft.com")!,
      password: password.value,
    });

    console.log("Authorization completed");
    router.push("/projects");
  } catch (error) {
    console.log(error)
    errorMessage.value = "Ungültige Anmeldedaten oder Fehler beim Speichern des Passworts.";
  }
};
</script>

<template>
  <form @submit.prevent="handleAuth">
    <div class="form-group">
      <label for="username" class="auth-label">Email-Adresse:</label><br/>
      <input type="text" id="username" class="formAuth" v-model="email" disabled />
    </div>
    <div class="form-group">
      <label for="password" class="auth-label">Passwort:</label><br/>
      <input type="password" id="password" class="formAuth" v-model="password" required />
    </div>
    <button type="submit" id="submitButton">Weiter</button>
    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
  </form>
</template>

<style scoped>
.form-group {
  margin-bottom: 3vh;
}

form {
  margin-top: 5vh;
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

.error {
  color: red;
  margin-top: 1vh;
}
</style>