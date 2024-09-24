<script setup lang="ts">
import {ref} from 'vue';
import {logout} from "@/services/auth.service";

const profileImage = ref<File | null>(null);
const imageUrl = ref<string | null>(null);

const onImageSelected = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (file) {
    profileImage.value = file;
    imageUrl.value = URL.createObjectURL(file);
  }
};
</script>

<template>

  <div id="bigVGContainer">
    <div id="VGHeaderBox">
      <h1 id="vgHeading">Profil</h1>
    </div>

    <div id="profileContentBox">

      <div id="inputBoxen">

        <div id="inputContainer">
          <div class="inputBox">
            <label for="firstName">Vorname</label>
            <input type="text">
          </div>

          <div class="inputBox">
            <label for="lastName">Nachname</label>
            <input type="text">
          </div>
        </div>

        <div class="inputBox" style="margin: 4% 0;width: 50%">
          <label for="email">Email-Adresse</label>
          <input type="email">
        </div>

        <div id="inputContainer">
          <div class="inputBox">
            <label for="userGroup">Benutzergruppe</label>
            <input type="text">
          </div>

          <div class="inputBox">
            <label for="lastName">Abteilung</label>
            <input type="text">
          </div>
        </div>
      </div>

      <div id="profileImageBox">
        <label for="profileImage"><img id="profileImage" src="../assets/profil.jpg"> </label>
        <input type="file" accept="image/*" @change="onImageSelected">
        <div v-if="imageUrl" id="imagePreview">
          <img :src="imageUrl" alt="Profilbild Vorschau">
        </div>
      </div>

      <button id="logout" @click="logout()">Logout</button>
    </div>
  </div>
</template>

<style scoped>
#inputBoxen{
  display: flex;
  flex-direction: column;
  width: 50%;
  border: black solid 1px;
}
#profileImage {
  width: 10vw;
  border-radius: 100%;
}

.inputBox label {
  color: #848484;
}

.inputBox input {
  all: unset;
  border-bottom: #D9D9D9 solid 2px;
}

.inputBox {
  display: flex;
  flex-direction: column;
  width: 50%;
}

#inputContainer {
  display: flex;
  flex-direction: row;
}

#inputContainer div {
  margin-right: 5%;
}

#bigVGContainer {
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
}

#VGHeaderBox {
  display: flex;
  flex-direction: row;
  background-color: white;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#vgHeading {
  margin-left: 4%;
  margin-top: 2%;
  margin-bottom: 2%;
  font-size: 1.1em;
}

#profileContentBox {
  background-color: white;
  display: flex;
  flex-direction: row;
  height: 85%;
  margin-top: 3%;
  padding: 5% 6%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

</style>