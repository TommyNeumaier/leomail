<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const handleSubmit = () => {

}

const handleBack = () => {
  router.push({ name: 'projekte' });
}
</script>

<template>
  <div id="bigBox">
    <img id="pfeil" src="../assets/icons/pfeil-links-big.png" @click="handleBack">
    <div id="contentBox">
      <h3 id="headline">Neues Projekt</h3>

      <form>

        <div class="boxLabel">
          <label for="name" class="project-label">Name des Projekts</label><br>
        </div>
        <input type="text" id="name" class="projectForm" placeholder="z.Bsp. Maturaball-2025">

        <div class="boxLabel">
          <label for="mail" class="project-label">Mail-Adresse</label><br>
        </div>
        <input type="email" id="mail" class="projectForm" placeholder="z.Bsp. maturaball-2025@gmail.com">

        <div id="buttonBox">
          <button type="button" @click="handleSubmit" id="submitButton">Projekt erstellen</button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
#contentBox{
  margin-top: 1%;
}
#pfeil {
  width: 2vw;
  margin-top: 2%;
  margin-left: 2%;
}

#bigBox {
  width: 100%;
}

#headline {
  margin-left: 10%;
  padding-bottom: 3%;
}

form {
  display: flex;
  flex-direction: column;
  margin-left: 15%;
}

.boxLabel {
}

.project-label {
  color: #5A5A5A;
  font-size: 0.8em;
}

.projectForm::placeholder {
  color: #B3B3B3;
}

.projectForm {
  display: block;
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 50%;
  font-size: 0.5em;
  margin-bottom: 3%;
  background-color: #F6F6F6;
}

.projectForm:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#buttonBox {
  position: absolute; /* Added */
  top: 80vh; /* Adjust as needed */
  right: 15vw; /* Adjust as needed */
  width: 12vw;
  margin-top: 0; /* Removed margin-top */
}

#submitButton {
  all: unset;
  border-radius: 12px;
  padding: 1vh 0;
  background-color: #78A6FF;
  color: white;
  width: 100%;
  border: #78A6FF solid 1px;
  font-size: 0.8rem;
  text-align: center;
}

#submitButton:hover {
  background-color: rgba(75, 129, 253, 0.86);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#submitButton:disabled {
  background-color: lightgray;
  border-color: lightgray;;
}

#submitButton:disabled:hover {
  border-color: lightgray;
  box-shadow: none;
}

</style>