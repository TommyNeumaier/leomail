<script setup lang="ts">

import MailDetailComponent from "@/components/mail/MailDetailComponent.vue";
import HeaderComponent from "@/components/header/HeaderComponent.vue";
import NavComponent from "@/components/navigation/NavComponent.vue";
</script>

<template>
  <header-component></header-component>
  <div id="mail-mainBox">
    <nav-component></nav-component>
    <MailDetailComponent></MailDetailComponent>
  </div>
</template>

<style scoped>
#mail-mainBox{
  display: flex;
  flex-direction: row;
  width: 100%;
}
</style>
