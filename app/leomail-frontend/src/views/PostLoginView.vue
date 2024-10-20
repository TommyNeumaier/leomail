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
import Spinner from '@/components/Spinner.vue'; // Ensure you have a Spinner component

const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  try {
    const isOutlookAuthorized = await Service.getInstance().checkOutlookAuthorization();
    console.log(isOutlookAuthorized)

    if (isOutlookAuthorized) {
      // Update store state
      // Redirect to 'projects'
      router.replace({ name: 'projects' });
    } else {
      // Redirect to 'authorisation' to handle Outlook password submission
      router.replace({ name: 'authorisation' });
    }
  } catch (error) {
    console.error('Fehler bei der Outlook-Autorisierung:', error);
    // Optionally, handle the error (e.g., show a notification)
    // Redirect to 'authorisation' as a fallback
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