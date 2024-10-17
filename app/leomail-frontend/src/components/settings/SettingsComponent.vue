<script setup lang="ts">

</script>

<template>
  <div id="bigContentBox">
    <form @submit.prevent="saveForm">
      <!-- Projektname -->
      <div class="inputBox">
        <label for="projectname">Projektname</label>
        <input type="text" class="project-form" v-model="projectName">
      </div>

      <!-- Anzeigename -->
      <div class="inputBox">
        <label for="displayName">Anzeigename</label>
        <input type="text" class="project-form" v-model="displayName">
      </div>

      <!-- Kurzbeschreibung -->
      <div class="inputBox">
        <label for="description">Kurzbeschreibung</label>
        <textarea type="text" class="project-form" v-model="description"></textarea>
      </div>

      <!-- Mail-Adresse -->
      <div class="inputBox">
        <label for="mail">Mail-Adresse</label>
        <input type="email" class="project-form" v-model="mail">
      </div>

      <!-- Password -->
      <div class="inputBox">
        <label for="password">Password</label>
        <input type="password" class="project-form" v-model="password">
      </div>

      <!-- Benutzer (recipients) -->
      <div class="inputBox">
        <label for="recipients">Benutzer</label>
        <div class="project-form"></div>
      </div>

      <!-- Save Button -->
      <div class="buttonBox">
        <button type="submit" class="save">Speichern</button>
      </div>
    </form>
  </div>
</template>

<style scoped>
/* content */
#bigContentBox {
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  padding: 3% 5%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

.inputBox{
  display: flex;
  flex-direction: column;
  margin-top: 1%;
}
.project-form {
  display: block;
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 50vw;
  font-size: 0.8em;
  background-color: #F6F6F6;
}

/* save button */
.save{
  all: unset;
  padding: 1vh 3vh;
  border-radius: 12px;
  background-color: #78A6FF;
  color: white;
  border: #78A6FF solid 1px;
  font-size: 0.8rem;
  margin-top: 4%;
}

.save:hover {
  background-color: rgba(75, 129, 253, 0.86);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

.save:disabled {
  background-color: lightgray;
  border-color: lightgray;;
}

.save:disabled:hover {
  border-color: lightgray;
  box-shadow: none;
}
</style>