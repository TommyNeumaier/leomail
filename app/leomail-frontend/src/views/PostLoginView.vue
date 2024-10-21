<!-- src/views/PostLoginView.vue -->
<template>
  <div class="post-login">
    <Spinner /> <!-- Optional: Add a spinner or loading indicator -->
    <p>Überprüfung der Outlook-Autorisierung...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.store';
import { Service } from '@/services/service';
import Spinner from '@/components/Spinner.vue';

const router = useRouter();

onMounted(async () => {
  try {
    const isOutlookAuthorized = await Service.getInstance().checkOutlookAuthorization();
    if (isOutlookAuthorized) {
      router.replace({ name: 'projects' });
    } else {
      router.replace({ name: 'authorisation' });
    }
  } catch (error) {
    router.replace({ name: 'authorisation' });
  }
});
</script>

<style scoped>
.post-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
</style>