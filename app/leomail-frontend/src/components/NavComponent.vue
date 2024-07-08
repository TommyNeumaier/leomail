<script setup lang="ts">

import {RouterLink} from "vue-router";
</script>

<template>
<div id="bigNavBox">
  <div id="iconLinks">
    <div class="iconBox">
      <RouterLink to="/">Mail</RouterLink>
    </div>
    <div class="iconBox">
      <RouterLink to="/geplanteMails">Geplante Mails</RouterLink>
    </div>
    <div class="iconBox">
      <RouterLink to="/geplanteMails">Groups</RouterLink>
    </div>
    <div class="iconBox">
      <RouterLink to="/geplanteMails">Vorlagen</RouterLink>
    </div>
    <div class="iconBoxSetting">
      <RouterLink to="/geplanteMails">Settings</RouterLink>
    </div>
  </div>
</div>

</template>

<style scoped>
#bigNavBox{
  width: 12%;
  height: 100%;
  border: black solid 1px;
  margin-top: 2%;
}
.iconBox{
  text-align: center;
  padding-top: 20%;
}
.iconBoxSetting{
  margin-top: 30rem;
  text-align: center;
}
</style>