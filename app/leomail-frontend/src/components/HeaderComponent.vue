<script setup lang="ts">

</script>

<template>
<div id="headerNav">
  <img alt="LeoMail-Logo" class="logo" src="@/assets/LeoMail.png" width="auto" height="45">
  <div id="HeaderLinkBox">
    <a class="HeaderLinks" href="#">Projekte</a>
    <a class="HeaderLinks" href="#">Personen</a>
  </div>
</div>
</template>

<style scoped>
body{
  margin: 0;
  padding: 0;
}
#headerNav{
  display: flex;
  flex-direction: row;
  height: 5em;
  padding-top: 15px;
  padding-left: 2.5%;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}
.HeaderLinks{
  border-left: black solid 1px;
  border-right: black solid 1px;
  padding: 10%;
  padding-left: 50%;
  padding-right: 50%;
  text-decoration: none;
  color: #1B1B1B;
}
#HeaderLinkBox a{
  font-size: 0.9em;
}
#HeaderLinkBox{
  margin-left: 4%;
  padding-top: 10px;
}
</style>