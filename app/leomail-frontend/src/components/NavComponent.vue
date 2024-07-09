<script setup lang="ts">

</script>

<template>
<div id="bigNavBox">
  <div id="iconLinks">
    <div class="iconBox">
      <RouterLink to="/" activeClass="highlight"><img src="../assets/icons/mail.svg" alt="Mail Icon" /></RouterLink>
    </div>
    <div class="iconBox">
      <RouterLink to="/geplanteMails" activeClass="highlight"><img src="../assets/icons/sceduled-mail.svg" alt="Sceduled Mail Icon" /></RouterLink>
    </div>
    <div class="iconBox">
      <RouterLink to="/gruppen" activeClass="highlight"><img src="../assets/icons/gruppe.svg" alt="Group Icon" /></RouterLink>
    </div>
    <div class="iconBox">
      <RouterLink to="/vorlagen" activeClass="highlight"><img src="../assets/icons/dokument.svg" alt="Vorlagen Icon" /></RouterLink>
    </div>
    <div class="iconBox">
      <RouterLink to="/settings" activeClass="highlight"><img src="../assets/icons/settings.svg" alt="Settings Icon" /></RouterLink>
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
.iconBoxSetting{
  margin-top: 240%;
  text-align: center;
}
#iconLinks{
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.iconBox {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 5%;
  width: 80%; /* Adjust width as needed */
  height: 100px; /* Adjust height as needed */
  //border: red solid 2px;
}

.iconBox img {
  width: 40px;
  padding: 20%;
}

.highlight {
  background-color: lightgray;
  border-radius: 80%;
  /*display: flex;
  align-items: center;
  justify-content: center;*/
}
</style>