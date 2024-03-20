package com.chatcallapp.chatcallfirebase.point

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.chatcallapp.chatcallfirebase.activity.HomeActivity
import com.chatcallapp.chatcallfirebase.databinding.ActivityPointListBinding
import com.chatcallapp.chatcallfirebase.model.ClientPackage
import com.chatcallapp.chatcallfirebase.model.ServerPackage


class PointListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPointListBinding
    private var billingClient: BillingClient? = null
    private var clientPackageList: MutableList<ClientPackage> = mutableListOf()
    private var getProductIdList: MutableList<String> = mutableListOf()
    private val mProductDetails: MutableList<ProductDetails> = ArrayList()
    private var pointAdapter: PointAdapter? = null
    private var clickPosition = -1
    private var transactionId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPointListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initEvent()

    }

    private fun initView() {
        setupPayment()
    }

    private fun initEvent() {
        binding.imgBack.setOnClickListener {
            val intent = Intent(this@PointListActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupPayment() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                billingClient?.startConnection(this)
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    requestListPointPackage()
                }
            }
        })
    }

    fun requestListPointPackage() {
        val listServerPackage = ArrayList<ServerPackage>()
        listServerPackage.add(ServerPackage(productId = "point_package_chat_01"))
        listServerPackage.add(ServerPackage(productId = "point_package_chat_02"))
        listServerPackage.add(ServerPackage(productId = "point_package_chat_03"))
        listServerPackage.add(ServerPackage(productId = "point_package_chat_04"))

        addPointClientPackage(listServerPackage)

        if (getProductIdList.isEmpty()) return
        val productsList = getProductIdList.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params =
            QueryProductDetailsParams.newBuilder().setProductList(productsList).build()
        billingClient?.queryProductDetailsAsync(params) { billingResult: BillingResult, productDetailsList ->

            val list: MutableList<ClientPackage> =
                mergePointPackageWithProductDetails(clientPackageList, productDetailsList)
            clientPackageList.clear()
            clientPackageList.addAll(list)

            Log.d("GGGG", "list point size: ${clientPackageList.size}")

            runOnUiThread { fillPointPackageToList(clientPackageList) }
        }
    }


    private fun addPointClientPackage(packages: List<ServerPackage>?) {
        var clientPackage: ClientPackage
        packages?.forEach {
            clientPackage = ClientPackage(it.packageId, it.point ?: 0, it.productionId)
            clientPackage.serverPrice = "${it.price}"
            clientPackageList.add(clientPackage)
            getProductIdList.add(it.productionId ?: "")
        }
    }

    private fun fillPointPackageToList(clientPackageList: MutableList<ClientPackage>) {
        binding.tvListEmpty.isVisible = clientPackageList.isEmpty()
        pointAdapter = PointAdapter(clientPackageList) {
            clickPosition = it
            startPayment()
        }
        binding.recyclerView.adapter = pointAdapter
        pointAdapter?.notifyDataSetChanged()
    }

    private fun startPayment() {
        try {
            val productDetailsParams = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(mProductDetails[clickPosition])
                    .build()
            )
            val billingFlowParams: BillingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParams)
                .build()
            billingClient?.launchBillingFlow(this, billingFlowParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult: BillingResult, purchases: List<Purchase?>? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchases != null) {
                    for (purchase in purchases) {
                        Log.d("GGGG", "BillingClient.BillingResponseCode.OK")
                        purchase?.let {
                            handlePurchase(it)
                        }
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.d("GGGG", "Handle an error caused by a user cancelling the purchase flow")
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                Log.d("GGGG", "ITEM_ALREADY_OWNED")
            } else {
                Log.d("GGGG", "False " + billingResult.responseCode)
            }
        }

    private fun handlePurchase(purchase: Purchase) {
        val consumeParams =
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        val listener =
            ConsumeResponseListener { billingResult: BillingResult, purchaseToken: String? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    runOnUiThread {
                        Toast.makeText(this, "Buy point success", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        billingClient!!.consumeAsync(consumeParams, listener)
    }

    private fun mergePointPackageWithProductDetails(
        pointPackages: MutableList<ClientPackage>?,
        productDetails: MutableList<ProductDetails>
    ): MutableList<ClientPackage> {
        val mergedList = mutableListOf<ClientPackage>()
        productDetails.forEach { productDetail ->
            pointPackages?.find {
                it.productId == productDetail.productId
            }?.let { pointPackage ->
                pointPackage.productDetails = productDetail
                pointPackage.amount =
                    productDetail.oneTimePurchaseOfferDetails?.priceAmountMicros?.toDouble()
                        ?.div(1_000_000) ?: 0.0
                pointPackage.currency =
                    productDetail.oneTimePurchaseOfferDetails?.priceCurrencyCode.orEmpty()
                pointPackage.description = productDetail.description
                mergedList.add(pointPackage)
                mProductDetails.add(productDetail)
            }
        }
        return mergedList
    }
}

// TODO: Setup payment
// TODO: Request len server lay goi point
// TODO: Khi co response thi merge point
// TODO: Update len list
// TODO: Click item list -> show popup mua point