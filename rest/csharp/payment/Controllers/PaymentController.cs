using CamundaClient;
using CamundaClient.Dto;
using CamundaClient.Worker;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;
using Polly;
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text;
using System.Threading;

namespace FlowingRetailPayment.Controllers
{
    public class PaymentControllerV1 : ControllerBase
    {
        [HttpPut]
        [Route("/api/payment/v1")]
        public IActionResult RetrievePayment([FromBody]String parameters)
        {
            var traceId = Guid.NewGuid().ToString();
            var customerId = "0815"; // TODO: get somehow from retrievePaymentPayload
            var amount = 15; // TODO get somehow from retrievePaymentPayload

            var transactionId = ChargeCreditCard(customerId, amount);

            var result = new Dictionary<String, String>();
            result.Add("status", "completed");
            result.Add("traceId", traceId);
            result.Add("transactionId", transactionId);
            return Ok(result);
        }

        public String ChargeCreditCard(string customerId, int amount)
        {
            return RestApiClient.InvokeRestApi(customerId, amount);
        }
    }

    public class RestApiClient
    {
        private static string STRIPE_CHARGE_URL = "http://localhost:8099/charge";

        public static String InvokeRestApi(string customerId, long amount, TimeSpan? timeout = null)
        {
            Console.WriteLine(DateTime.Now.ToString("HH:mm:ss.fff") + " INVOKE STRIPE");

            var createChargeRequest = new CreateChargeRequest();
            createChargeRequest.amount = amount;

            HttpClient client = new HttpClient();
            if (timeout!=null)
            {
                client.Timeout = timeout.Value;
            }
            var httpResponse = client.PostAsync(STRIPE_CHARGE_URL, new StringContent(JsonConvert.SerializeObject(createChargeRequest), Encoding.UTF8, "application/json")).Result;
            httpResponse.EnsureSuccessStatusCode();

            var createChargeResponse = JsonConvert.DeserializeObject<CreateChargeResponse>(httpResponse.Content.ReadAsStringAsync().Result);
            return createChargeResponse.transactionId;
        }

        public class CreateChargeRequest
        {
            public long amount;
        }
        public class CreateChargeResponse
        {
            public String transactionId;
        }
    }

    public class PaymentControllerV2 : ControllerBase
    {

        static Policy circuitBreakerPolicy = Policy
                .Handle<Exception>()
                .CircuitBreaker(
                    exceptionsAllowedBeforeBreaking: 2,
                    durationOfBreak: TimeSpan.FromSeconds(5)
                );

        [HttpPut]
        [Route("/api/payment/v2")]
        public IActionResult RetrievePayment([FromBody]String parameters)
        {
            var traceId = Guid.NewGuid().ToString();
            var customerId = "0815"; // TODO: get somehow from retrievePaymentPayload
            var amount = 15; // TODO get somehow from retrievePaymentPayload

            var transactionId = ChargeCreditCard(customerId, amount);

            var result = new Dictionary<String, String>();
            result.Add("status", "completed");
            result.Add("traceId", traceId);
            result.Add("transactionId", transactionId);
            return Ok(result);
        }

        public String ChargeCreditCard(string customerId, int amount)
        {            
           return circuitBreakerPolicy.Execute<String>(
               () => RestApiClient.InvokeRestApi(customerId, amount, TimeSpan.FromSeconds(1))
           );
        }       

    }

    public class PaymentControllerV3 : ControllerBase
    {

        static Policy circuitBreakerPolicy = Policy
                .Handle<Exception>()
                .CircuitBreaker(
                    exceptionsAllowedBeforeBreaking: 2,
                    durationOfBreak: TimeSpan.FromSeconds(5)
                );

        [HttpPut]
        [Route("/api/payment/v3")]
        public IActionResult RetrievePayment([FromBody]String parameters)
        {
            var traceId = Guid.NewGuid().ToString();
            var customerId = "0815"; // TODO: get somehow from retrievePaymentPayload
            var amount = 15; // TODO get somehow from retrievePaymentPayload

            ChargeCreditCard(customerId, amount);

            var result = new Dictionary<String, String>();
            result.Add("status", "pending");
            result.Add("traceId", traceId);
            return Accepted(result);
        }

        public void ChargeCreditCard(string customerId, int amount)
        {
            Program.Camunda.BpmnWorkflowService.StartProcessInstance("paymentV3.cs", new Dictionary<string, object>  {
                {"customerId", customerId},
                {"amount", amount}
            });
        }

        [ExternalTaskTopic("chargeCreditCardV3")]
        [ExternalTaskVariableRequirements("customerId", "amount")]
        class InformCustomerSuccessAdapter : IExternalTaskAdapter
        {
            public void Execute(ExternalTask externalTask, ref Dictionary<string, object> resultVariables)
            {
                string customerId = (string)externalTask.Variables["customerId"].Value;
                long amount = (long)externalTask.Variables["amount"].Value;
                String transactionId = circuitBreakerPolicy.Execute<String>(
                    () => RestApiClient.InvokeRestApi(customerId, amount, TimeSpan.FromSeconds(1)));
                resultVariables.Add("transactionId", transactionId);
            }
        }
    }

    public class PaymentControllerV4 : ControllerBase
    {
        static Policy circuitBreakerPolicy = Policy
                .Handle<Exception>()
                .CircuitBreaker(
                    exceptionsAllowedBeforeBreaking: 2,
                    durationOfBreak: TimeSpan.FromSeconds(5)
                );

        [HttpPut]
        [Route("/api/payment/v4")]
        public IActionResult RetrievePayment([FromBody]String parameters)
        {
            Console.WriteLine(DateTime.Now.ToString("HH:mm:ss.fff") + " GOT REQUEST");

            var traceId = Guid.NewGuid().ToString();
            var customerId = "0815"; // TODO: get somehow from retrievePaymentPayload
            var amount = 15; // TODO get somehow from retrievePaymentPayload

            SemaphoreSlim semaphore = new SemaphoreSlim(1);
            semaphore.Wait(); // aqcuire and release later if everything is done
            semaphors.Add(traceId, semaphore);
            ChargeCreditCard(traceId, customerId, amount);

            var result = new Dictionary<String, String>();
            result.Add("traceId", traceId);
            if (semaphore.Wait(TimeSpan.FromMilliseconds(500)))
            {
                result.Add("status", "completed");
                return Ok(result); // HTTP 200
            }
            else
            {
                result.Add("status", "pending");
                return Accepted(result); // HTTP 202
            }
        }

        public void ChargeCreditCard(String traceId, string customerId, int amount)
        {
            Console.WriteLine(DateTime.Now.ToString("HH:mm:ss.fff") + " START WORKFLOW");
            Program.Camunda.BpmnWorkflowService.StartProcessInstance("paymentV4.cs", new Dictionary<string, object>  {
                {"customerId", customerId},
                {"amount", amount},
                {"traceId", traceId }
            });
            Console.WriteLine(DateTime.Now.ToString("HH:mm:ss.fff") + " STARTED WORKFLOW");
        }

        [ExternalTaskTopic("chargeCreditCardV4")]
        [ExternalTaskVariableRequirements("customerId", "amount", "traceId")]
        class InformCustomerSuccessAdapter : IExternalTaskAdapter
        {
            public void Execute(ExternalTask externalTask, ref Dictionary<string, object> resultVariables)
            {
                Console.WriteLine(DateTime.Now.ToString("HH:mm:ss.fff") + " EXECUTE EXTERNAL TASK");

                string customerId = (string)externalTask.Variables["customerId"].Value;
                long amount = (long)externalTask.Variables["amount"].Value;
                String transactionId = circuitBreakerPolicy.Execute<String>(
                    () => RestApiClient.InvokeRestApi(customerId, amount, TimeSpan.FromSeconds(1)));
                resultVariables.Add("transactionId", transactionId);

                string traceId = (string)externalTask.Variables["traceId"].Value;
                if (semaphors.ContainsKey(traceId))
                {
                    semaphors[traceId].Release();
                }
                Console.WriteLine(DateTime.Now.ToString("HH:mm:ss.fff") + " EXECUTED EXTERNAL TASK");
            }
        }

        public static Dictionary<String, SemaphoreSlim> semaphors = new Dictionary<String, SemaphoreSlim>();

    }
}
