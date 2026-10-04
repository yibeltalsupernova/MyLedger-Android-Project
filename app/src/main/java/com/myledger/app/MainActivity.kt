package com.myledger.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.myledger.app.data.local.*
import com.myledger.app.data.parser.CategoryEngine
import com.myledger.app.export.CsvExporter
import com.myledger.app.security.AppSettings
import com.myledger.app.ui.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:ComponentActivity(){
    private val vm:MainViewModel by viewModels{MainViewModel.Factory((application as MyLedgerApplication).repository)}
    private val permissions=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){}
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        if(listOf(Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_SMS).any{
            ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED
        }) permissions.launch(arrayOf(Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_SMS))
        setContent{App(vm)}
    }
}

@Composable fun App(vm:MainViewModel){
    var tab by remember{mutableIntStateOf(0)}
    var add by remember{mutableStateOf(false)}
    var settings by remember{mutableStateOf(false)}
    var search by remember{mutableStateOf("")}
    val tx by vm.filtered.collectAsState()
    val income by vm.income.collectAsState()
    val expense by vm.expense.collectAsState()
    val cats by vm.categoryTotals.collectAsState()
    val budgets by vm.budgets.collectAsState()
    val context=androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar={TopAppBar(
            title={Column{
                Text("MyLedger",fontWeight=FontWeight.Bold)
                Text("ማህረቤ",style=MaterialTheme.typography.labelSmall)
            }},
            actions={
                IconButton({settings=true}){Icon(Icons.Default.Settings,"Settings")}
            }
        )},
        bottomBar={NavigationBar{
            listOf("Home","Transactions","Budgets","Insights").forEachIndexed{i,label->
                NavigationBarItem(
                    selected=tab==i,onClick={tab=i},
                    icon={Icon(
                        when(i){0->Icons.Default.Home;1->Icons.Default.List;2->Icons.Default.AccountBalanceWallet;else->Icons.Default.BarChart},
                        label
                    )},label={Text(label)}
                )
            }
        }},
        floatingActionButton={
            if(tab<3) FloatingActionButton({add=true}){Icon(Icons.Default.Add,"Add")}
        }
    ){padding->
        when(tab){
            0->Home(tx,income,expense,cats,padding)
            1->Transactions(tx,search,{search=it;vm.search(it)}, {vm.delete(it)},padding)
            2->Budgets(budgets,{vm.saveBudget(it)},{vm.deleteBudget(it)},padding)
            3->Insights(cats,expense,padding)
        }
    }
    if(add) AddDialog({add=false}){vm.add(it);add=false}
    if(settings) SettingsDialog({settings=false},tx,context)
}

@Composable fun Home(
    tx:List<TransactionEntity>,income:Double,expense:Double,
    cats:List<com.myledger.app.data.local.CategoryTotal>,padding:PaddingValues
){
    LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{BalanceCard(income,expense)}
        item{Text("Quick summary",style=MaterialTheme.typography.titleLarge)}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
            SmallCard("Transactions",tx.size.toString(),Modifier.weight(1f))
            SmallCard("Categories",cats.size.toString(),Modifier.weight(1f))
        }}
        item{Text("Recent activity",style=MaterialTheme.typography.titleLarge)}
        items(tx.take(7),key={it.id}){TransactionRow(it,{})}
        if(tx.isEmpty())item{EmptyState()}
    }
}

@Composable fun Transactions(
    tx:List<TransactionEntity>,search:String,onSearch:(String)->Unit,
    delete:(TransactionEntity)->Unit,padding:PaddingValues
){
    LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{OutlinedTextField(search,onSearch,Modifier.fillMaxWidth(),label={Text("Search")},singleLine=true)}
        item{Text("${tx.size} transactions",style=MaterialTheme.typography.titleMedium)}
        items(tx,key={it.id}){TransactionRow(it,{delete(it)})}
        if(tx.isEmpty())item{EmptyState()}
    }
}

@Composable fun Budgets(
    budgets:List<BudgetEntity>,save:(BudgetEntity)->Unit,delete:(BudgetEntity)->Unit,padding:PaddingValues
){
    var dialog by remember{mutableStateOf(false)}
    LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
            Text("Monthly budgets",style=MaterialTheme.typography.titleLarge)
            Button({dialog=true}){Text("Add")}
        }}
        items(budgets,key={it.id}){b->BudgetRow(b,{delete(b)})}
        if(budgets.isEmpty())item{Text("Create category limits to control monthly spending.")}
    }
    if(dialog)BudgetDialog({dialog=false}){save(it);dialog=false}
}

@Composable fun Insights(
    cats:List<com.myledger.app.data.local.CategoryTotal>,expense:Double,padding:PaddingValues
){
    LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Text("Spending insights",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)}
        item{Text("Total expenses: ${money(expense)}")}
        if(cats.isEmpty())item{EmptyState()}
        cats.forEach{c->item{InsightRow(c.category.name,c.total,expense)}}
    }
}

@Composable fun BalanceCard(income:Double,expense:Double){
    Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp)){
        Column(Modifier.padding(22.dp)){
            Text("Available balance",style=MaterialTheme.typography.labelLarge)
            Text(money(income-expense),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                Text("Income\n${money(income)}")
                Text("Expenses\n${money(expense)}")
            }
        }
    }
}
@Composable fun SmallCard(title:String,value:String,modifier:Modifier){
    Card(modifier){Column(Modifier.padding(16.dp)){Text(title,style=MaterialTheme.typography.labelMedium);Text(value,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}}
}
@Composable fun TransactionRow(t:TransactionEntity,onDelete:()->Unit){
    Card(Modifier.fillMaxWidth()){
        Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f)){
                Text(t.vendor,fontWeight=FontWeight.SemiBold)
                Text("${t.category.name.replace('_',' ')} • ${t.source.name}",style=MaterialTheme.typography.bodySmall)
                Text(SimpleDateFormat("MMM d, yyyy • h:mm a",Locale.getDefault()).format(Date(t.timestamp)),style=MaterialTheme.typography.labelSmall)
                if(t.isAutoParsed)Text("Detected from SMS",style=MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment=Alignment.End){
                Text((if(t.type==TransactionType.INCOME)"+ " else if(t.type==TransactionType.EXPENSE)"- " else "↔ ")+money(t.amount),fontWeight=FontWeight.Bold)
                TextButton(onDelete){Text("Delete")}
            }
        }
    }
}
@Composable fun BudgetRow(b:BudgetEntity,onDelete:()->Unit){
    Card(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){
        Column(Modifier.weight(1f)){Text(b.category.name.replace('_',' '),fontWeight=FontWeight.SemiBold);Text("Limit: ${money(b.monthlyLimit)} • ${b.monthKey}") }
        TextButton(onDelete){Text("Delete")}
    }}
}
@Composable fun InsightRow(name:String,total:Double,all:Double){
    val f=if(all>0)(total/all).toFloat() else 0f
    Column{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(name.replace('_',' '));Text(money(total))};LinearProgressIndicator(f,Modifier.fillMaxWidth())}
}
@Composable fun EmptyState(){Card(Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("Nothing here yet",fontWeight=FontWeight.Bold);Text("Add a transaction or enable SMS detection.")}}}

@Composable fun AddDialog(dismiss:()->Unit,add:(TransactionEntity)->Unit){
    var vendor by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};var expense by remember{mutableStateOf(true)}
    AlertDialog(onDismissRequest=dismiss,title={Text("New transaction")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        OutlinedTextField(vendor,{vendor=it},label={Text("Vendor / description")},singleLine=true)
        OutlinedTextField(amount,{amount=it},label={Text("Amount ETB")},singleLine=true)
        Row{FilterChip(expense,{expense=true},label={Text("Expense")});Spacer(Modifier.width(8.dp));FilterChip(!expense,{expense=false},label={Text("Income")})}
        Text("Auto category: ${CategoryEngine.categorize(vendor).name.replace('_',' ')}",style=MaterialTheme.typography.labelMedium)
    }},confirmButton={TextButton({
        val v=amount.replace(",","").toDoubleOrNull()
        if(v!=null&&v>0)add(TransactionEntity(amount=v,type=if(expense)TransactionType.EXPENSE else TransactionType.INCOME,vendor=vendor.ifBlank{"Manual transaction"},category=if(expense)CategoryEngine.categorize(vendor) else ExpenseCategory.SALARY,source=PaymentSource.MANUAL))
    }){Text("Save")}},dismissButton={TextButton(dismiss){Text("Cancel")}})
}
@Composable fun BudgetDialog(dismiss:()->Unit,save:(BudgetEntity)->Unit){
    var category by remember{mutableStateOf(ExpenseCategory.FOOD)};var limit by remember{mutableStateOf("")}
    AlertDialog(onDismissRequest=dismiss,title={Text("Add monthly budget")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text("Category")
        LazyRow(horizontalArrangement=Arrangement.spacedBy(5.dp)){items(ExpenseCategory.entries.filter{it!=ExpenseCategory.SALARY&&it!=ExpenseCategory.TRANSFER}){c->FilterChip(category==c,{category=c},label={Text(c.name.replace('_',' '))})}}
        OutlinedTextField(limit,{limit=it},label={Text("Monthly limit ETB")},singleLine=true)
    }},confirmButton={TextButton({
        val v=limit.toDoubleOrNull();if(v!=null&&v>0)save(BudgetEntity(category=category,monthlyLimit=v,monthKey=SimpleDateFormat("yyyy-MM",Locale.US).format(Date())))
    }){Text("Save")}},dismissButton={TextButton(dismiss){Text("Cancel")}})
}
@Composable fun SettingsDialog(dismiss:()->Unit,tx:List<TransactionEntity>,context:android.content.Context){
    var dark by remember{mutableStateOf(false)}
    AlertDialog(onDismissRequest=dismiss,title={Text("Settings")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("Local-first: your transactions are stored on this device.")
        Row(verticalAlignment=Alignment.CenterVertically){Text("Dark mode",Modifier.weight(1f));Switch(dark,{dark=it})}
        Text("SMS monitoring requires Android SMS permissions.")
        Text("MyLedger 1.2.0",style=MaterialTheme.typography.labelSmall)
    }},confirmButton={
        Row{TextButton({CsvExporter.share(context,tx)}){Text("Export CSV")};TextButton(dismiss){Text("Close")}}
    })
}
private fun money(v:Double)=NumberFormat.getNumberInstance(Locale.US).format(v)+" ETB"
