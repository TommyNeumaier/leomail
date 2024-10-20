<template>
  <div id="mailDetailContainer">
    <h1>Email Detail</h1>
    <div v-if="mailDetail">
      <h2>{{ mailDetail.meta.mailHeadline }}</h2>
      <p><strong>Gesendet von:</strong> {{ name }}</p>
      <p><strong>Gesendet am:</strong> {{ mailDetail.keyDates.sentOn }}</p>
      <div>
        <h3>Inhalt:</h3>
        <p>{{ mailDetail.meta.mailContent }}</p>
      </div>
      <div v-for="(mailDetailItem, index) in mailDetail.mails" :key="index">
        <p><strong>Kontakt:</strong> {{ mailDetailItem.contact.firstName }} {{ mailDetailItem.contact.lastName }}</p>
        <p><strong>Email:</strong> {{ mailDetailItem.contact.mailAddress }}</p>
        <p><strong>Inhalt:</strong> {{ mailDetailItem.content }}</p>
      </div>
    </div>
    <div v-else>
      <p>Lade E-Mail...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Service } from "@/services/service";

const route = useRoute();
const mailDetail = ref(null);

const mailId = ref(route.params.id);
const projectId = ref(route.params.projectId);
const name = ref();

const getProfile = async () => {
  const response = await Service.getInstance().getProfile();
  name.value = response.data.firstName + " " + response.data.lastName;
  console.log(name.value);
};

const fetchMailDetail = async () => {
    const response = await Service.getInstance().getUsedTemplate(mailId.value, projectId.value);  // Die Service-Methode angepasst, um auch die projectId zu berücksichtigen
    console.log(response.data);
    mailDetail.value = response.data;
};

onMounted(() => {
  console.log(route.params.id);
  console.log(route.params.projectId);
  fetchMailDetail();
  getProfile();
});
</script>

<style scoped>
/* Dein Stil bleibt gleich */
</style>
