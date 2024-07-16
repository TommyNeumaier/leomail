<script setup lang="ts">

import {ref} from "vue";

const picked = ref<string>(''); // Holds the value of the selected gender
const pickedEntity = ref<string>(''); // Holds the value of the selected gender

const handleGenderChange = (gender: string) => {
  picked.value = gender;
};

const handleEntities = (entity: string) => {
  pickedEntity.value = entity;
};
</script>

<template>
  <form>
    <h3>Neue Person</h3>
    <div>
      <label for="checkboxMale">männlich</label>
      <input type="checkbox" class="checkbox" id="checkboxMale" value="male" @change="handleGenderChange('male')"
             :checked="picked === 'male'"/>

      <label for="checkboxFemale">weiblich</label>
      <input type="checkbox" class="checkbox" id="checkboxFemale" value="female" @change="handleGenderChange('female')"
             :checked="picked === 'female'"/>
    </div>

    <div>
      <label for="titel" class="template-label">Titel (optional)</label><br>
      <input type="text" id="titel" class="formPerson">
    </div>
    <div>
      <label for="firstName" class="template-label">Vorname</label><br>
      <input type="text" id="firstName" class="formPerson">
    </div>
    <div>
      <label for="lastName" class="template-label">Titel (optional)</label><br>
      <input type="text" id="lastName" class="formPerson">
    </div>
    <div>
      <label for="email" class="template-label">Email</label><br>
      <input type="email" id="email" class="formPerson" placeholder="z.Bsp. max.muster@gmail.com">
    </div>
    <div>
      <label for="checkboxUnternehmen">Unternehmen</label>
      <input type="checkbox" class="checkbox" id="checkboxUnternehmen" value="unternehmen"
             @change="handleEntities('unternehmen')"
             :checked="pickedEntity === 'unternehmen'"/>

      <label for="checkboxPrivatperson">Privatperson</label>
      <input type="checkbox" class="checkbox" id="checkboxPrivatperson" value="privatperson"
             @change="handleEntities('privatperson')"
             :checked="pickedEntity === 'privatperson'"/>
    </div>

    <div>
      <label for="company" class="template-label">Firma (optional)</label><br>
      <input type="text" id="company" class="formPerson">
    </div>
    <div>
      <label for="position" class="template-label">Position (optional)</label><br>
      <!-- wenn Firma eingegeben wurde dann muss die position auch-->
      <input type="text" id="company" class="formPerson">
    </div>
  </form>
</template>

<style scoped>

</style>