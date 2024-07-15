<script setup lang="ts">
import MainComponent from "@/components/MailsComponent.vue";
import HeaderComponent from "@/components/HeaderComponent.vue";
import NavComponent from "@/components/NavComponent.vue";
</script>

<template>
  <header-component></header-component>
  <div id="mail-mainBox">
    <nav-component></nav-component>
    <main-component></main-component>
  </div>
</template>

<style scoped>
#mail-mainBox{
  display: flex;
  flex-direction: row;
}
</style>