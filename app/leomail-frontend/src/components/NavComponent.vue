<script setup lang="ts">
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
  width: 10%;
  margin-top: 2%;
  border-right: rgba(0, 0, 0, 0.20) solid 2px;
  border-radius: 20px;
  padding-bottom: 1.5%;
}
.iconBox{
  text-align: center;
  padding-top: 20%;
}
.iconBoxSetting{
  margin-top: 240%;
  text-align: center;
}
</style>