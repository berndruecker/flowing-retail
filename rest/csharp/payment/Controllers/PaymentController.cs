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
using static FlowingRetailPayment.Controllers.RestApiClient;

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
            return RestApiClient.InvokeRestApi(customerId, amount).transactionId;
        }
    }

    public class RestApiClient
    {
        private static string STRIPE_CHARGE_URL = "http://localhost:8099/charge";

        public static CreateChargeResponse InvokeRestApi(string customerId, long amount, TimeSpan? timeout = null)
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

            return JsonConvert.DeserializeObject<CreateChargeResponse>(httpResponse.Content.ReadAsStringAsync().Result);
        }

        public class CreateChargeRequest
        {
            public long amount;
        }
        public class CreateChargeResponse
        {
            public String transactionId;
            public String errorCode;
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
               () => RestApiClient.InvokeRestApi(customerId, amount, TimeSpan.FromSeconds(1)).transactionId
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
                    () => RestApiClient.InvokeRestApi(customerId, amount, TimeSpan.FromSeconds(1)).transactionId);
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
            Program.Camunda.BpmnWorkflowService.StartProcessInstance("paymentV4.cs", new Dictionary<string, object>  {
                {"customerId", customerId},
                {"amount", amount},
                {"traceId", traceId }
            });
        }

        [ExternalTaskTopic("chargeCreditCardV4")]
        [ExternalTaskVariableRequirements("customerId", "amount", "traceId")]
        class InformCustomerSuccessAdapter : IExternalTaskAdapter
        {
            public void Execute(ExternalTask externalTask, ref Dictionary<string, object> resultVariables)
            {
                string customerId = (string)externalTask.Variables["customerId"].Value;
                long amount = (long)externalTask.Variables["amount"].Value;
                String transactionId = circuitBreakerPolicy.Execute<String>(
                    () => RestApiClient.InvokeRestApi(customerId, amount, TimeSpan.FromSeconds(1)).transactionId);
                resultVariables.Add("transactionId", transactionId);

                string traceId = (string)externalTask.Variables["traceId"].Value;
                if (semaphors.ContainsKey(traceId))
                {
                    semaphors[traceId].Release();
                }
            }
        }

        public static Dictionary<String, SemaphoreSlim> semaphors = new Dictionary<String, SemaphoreSlim>();

    }

    public class PaymentControllerV6 : ControllerBase
    {
        static Policy circuitBreakerPolicy = Policy
                .Handle<Exception>()
                .CircuitBreaker(
                    exceptionsAllowedBeforeBreaking: 2,
                    durationOfBreak: TimeSpan.FromSeconds(5)
                );

        [HttpPut]
        [Route("/api/payment/v6")]
        public IActionResult RetrievePayment([FromBody]String parameters)
        {
            var traceId = Guid.NewGuid().ToString();
            var customerId = "0815"; // TODO: get somehow from retrievePaymentPayload
            var amount = 15; // TODO get somehow from retrievePaymentPayload

            SemaphoreSlim semaphore = new SemaphoreSlim(1);
            semaphore.Wait(); // aqcuire and release later if everything is done
            semaphors.Add(traceId, semaphore);
            ChargeCreditCard(traceId, customerId, amount);

            var result = new Dictionary<String, String>();
            result.Add("traceId", traceId);
            if (semaphore.Wait(TimeSpan.FromMilliseconds(1000)))
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
            Program.Camunda.BpmnWorkflowService.StartProcessInstance("paymentV6.cs", new Dictionary<string, object>  {
                {"customerId", customerId},
                {"amount", amount},
                {"traceId", traceId }
            });
        }

        [ExternalTaskTopic("chargeCreditCardV6")]
        [ExternalTaskVariableRequirements("customerId", "amount", "traceId")]
        class InformCustomerSuccessAdapter : IExternalTaskAdapter
        {
            public void Execute(ExternalTask externalTask, ref Dictionary<string, object> resultVariables)
            {
                string customerId = (string)externalTask.Variables["customerId"].Value;
                long amount = (long)externalTask.Variables["amount"].Value;
                CreateChargeResponse response = circuitBreakerPolicy.Execute<CreateChargeResponse>(
                    () => RestApiClient.InvokeRestApi(customerId, amount, TimeSpan.FromSeconds(1)));

                if (!string.IsNullOrEmpty(response.errorCode))
                {
                    // raise error to be handled in BPMN model in case there was an error in credit card handling
                    throw new UnrecoverableBusinessErrorException("Error_CreditCardError", "Could not charge credit card");
                }

                resultVariables.Add("transactionId", response.transactionId);

                string traceId = (string)externalTask.Variables["traceId"].Value;
                if (semaphors.ContainsKey(traceId))
                {
                    semaphors[traceId].Release();
                }
            }
        }

        public static Dictionary<String, SemaphoreSlim> semaphors = new Dictionary<String, SemaphoreSlim>();

    }
}
