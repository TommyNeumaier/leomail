<script setup lang="ts">
import {QuillEditor} from "@vueup/vue-quill";
</script>

<template>
  <form @submit.prevent="addGroup">
    <div>
      <label for="name1" class="group-label">Gruppenname</label><br>
      <input v-model="inputName" type="text" id="name" class="formGroup"
             placeholder="Geben Sie einen Gruppennamen ein ...">
    </div>
    <div>
      <label for="groupDescription" class="group-label">Gruppenbeschreibung (optional)</label><br>
      <input v-model="inputGroupDescription" type="text" class="formGroup"
             placeholder="Geben Sie eine kurze Beschreibung zur Gruppe ..." id="groupDescription">
    </div>

    <button type="submit">Absenden</button>
  </form>

</template>

<style scoped>

</style>