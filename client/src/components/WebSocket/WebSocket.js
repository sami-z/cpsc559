export const REQUEST_QUEUE_PORT = 80;
export const RESPONSE_QUEUE_PORT = 9090;
// export const REQUEST_QUEUE_IPS = ["18.189.196.143","3.136.94.117"];
// export const RESPONSE_QUEUE_IPS = ["172.31.7.43","172.31.11.39"];
export const REQUEST_QUEUE_IPS = ["localhost"];
export const RESPONSE_QUEUE_IPS = ["localhost"];


export async function createWebSocket(ips, portNum) {

    for (const ip of ips) {
        const address = `ws://${ip}:${portNum}`;
        const ws = new WebSocket(address);

        const openPromise = new Promise((resolve, reject) => {
          ws.onopen = () => {
            resolve(ws);
          };
          ws.onerror = (event) => {
            reject(`Error occurred while connecting to ${address}: ${event}`);
          };
        });
    
        try {
          const result = await Promise.race([
            openPromise,
            new Promise((_, reject) => {
              setTimeout(() => reject(`Timeout occurred while connecting to ${address}`), 1000);
            }),
          ]);
    
          if (result) {
            return result;

          }
        } catch (error) {
          console.error(error);
          ws.close();
        }
      }
    
      throw new Error('No good WebSocket URLs found');
}
