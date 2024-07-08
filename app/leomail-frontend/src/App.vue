<script setup lang="ts">
import {RouterLink, RouterView} from 'vue-router'
import HeaderComponent from "@/components/HeaderComponent.vue";
import NavComponent from "@/components/NavComponent.vue";
</script>

<template>
 <link href='https://fonts.googleapis.com/css?family=Inter' rel='stylesheet'>

  <HeaderComponent></HeaderComponent>
  <nav-component></nav-component>

  <RouterView/>
</template>

<style scoped>
</style>
