import{e as j,r as n,j as e,L as l}from"./react-vendor-DcD85rEW.js";import{N as h}from"./Navigation-ShnYjyNJ.js";import"./vendor-IlC0tyTs.js";import"./index-DQxPBWdd.js";const u="pk_orders_v1";function b(){const{orderId:o}=j(),[s,c]=n.useState(null);n.useEffect(()=>{try{const t=localStorage.getItem(u),d=t?JSON.parse(t):[];c(d.find(i=>i.id===o)||null)}catch{c(null)}},[o]);const a=(t,d)=>new Intl.NumberFormat("en-US",{style:"currency",currency:d||"USD"}).format(t),m=n.useMemo(()=>s?.eta?`${new Date(s.eta).toLocaleDateString()} by 8:00 PM`:"TBD",[s?.eta]),p=async()=>{const t=window.location.href,d=`Order ${s?.id} placed on ${s?new Date(s.placedAt).toLocaleString():""}`;try{navigator.share?await navigator.share({title:"My Order",text:d,url:t}):navigator.clipboard&&(await navigator.clipboard.writeText(t),alert("Link copied to clipboard"))}catch{}},x=()=>{if(!s)return;const t=window.open("","_blank");if(!t)return;const d=s.items.map(r=>`
      <tr>
        <td style="padding:8px;border:1px solid #e5e7eb;">${r.name}</td>
        <td style="padding:8px;border:1px solid #e5e7eb;">${r.quantity}</td>
        <td style="padding:8px;border:1px solid #e5e7eb;">${a(r.unitPrice,s.totals.currency)}</td>
        <td style="padding:8px;border:1px solid #e5e7eb;">${a(r.lineTotal,s.totals.currency)}</td>
      </tr>
    `).join(""),i=`
      <html>
        <head>
          <title>Invoice ${s.id}</title>
          <meta charset="utf-8" />
          <style>
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; padding: 24px; }
            h1 { margin: 0 0 8px; }
            .muted { color: #6b7280; }
            table { width: 100%; border-collapse: collapse; margin-top: 16px; }
            th { text-align: left; background: #f9fafb; padding: 8px; border:1px solid #e5e7eb; }
            .totals { margin-top: 16px; float: right; }
            .totals div { display:flex; justify-content: space-between; gap: 24px; }
          </style>
        </head>
        <body>
          <h1>Invoice</h1>
          <div class="muted">Order ID: ${s.id}</div>
          <div class="muted">Placed on: ${new Date(s.placedAt).toLocaleString()}</div>
          <div class="muted">Payment Method: ${s.paymentMethod}</div>
          <h2 style="margin-top:16px;">Billing & Shipping</h2>
          <div>${s.address.fullName}</div>
          <div>${s.address.line1}${s.address.line2?", "+s.address.line2:""}</div>
          <div>${s.address.city}, ${s.address.state} ${s.address.postalCode}</div>
          <div>${s.address.country}</div>
          <table>
            <thead>
              <tr>
                <th>Item</th>
                <th>Qty</th>
                <th>Unit</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              ${d}
            </tbody>
          </table>
          <div class="totals">
            <div><span>Subtotal:</span><span>${a(s.totals.subtotal,s.totals.currency)}</span></div>
            <div><span>Shipping:</span><span>${a(s.totals.shipping,s.totals.currency)}</span></div>
            <div><strong>Total:</strong><strong>${a(s.totals.total,s.totals.currency)}</strong></div>
          </div>
          <script>window.onload = () => window.print();<\/script>
        </body>
      </html>
    `;t.document.write(i),t.document.close()};return s?e.jsxs("div",{className:"order-detail-page",children:[e.jsx(h,{}),e.jsxs("div",{className:"order-detail-container",children:[e.jsxs("div",{className:"header",children:[e.jsxs("div",{children:[e.jsx("h1",{children:"Order Details"}),e.jsxs("div",{className:"muted",children:["Order ID: ",s.id]})]}),e.jsx("span",{className:`status ${s.status.toLowerCase()}`,children:s.status})]}),e.jsxs("div",{className:"grid",children:[e.jsxs("section",{className:"card",children:[e.jsx("h2",{className:"section-title",children:"Delivery ETA"}),e.jsxs("p",{children:["Estimated delivery: ",e.jsx("strong",{children:m})]}),e.jsxs("p",{className:"muted",children:["Placed on ",new Date(s.placedAt).toLocaleString()]})]}),e.jsxs("section",{className:"card",children:[e.jsx("h2",{className:"section-title",children:"Shipping Address"}),e.jsx("div",{children:s.address.fullName}),e.jsxs("div",{children:[s.address.line1,s.address.line2?`, ${s.address.line2}`:""]}),e.jsxs("div",{children:[s.address.city,", ",s.address.state," ",s.address.postalCode]}),e.jsx("div",{children:s.address.country}),e.jsxs("div",{className:"muted",style:{marginTop:6},children:["Phone: ",s.address.phone]})]}),e.jsxs("section",{className:"card items",children:[e.jsx("h2",{className:"section-title",children:"Items"}),s.items.map(t=>e.jsxs("div",{className:"item-row",children:[e.jsxs("div",{className:"item-info",children:[e.jsx("img",{src:t.thumbnail_url,alt:t.name,onError:d=>{d.target.src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjQiIGhlaWdodD0iNjQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjY0IiBoZWlnaHQ9IjY0IiBmaWxsPSIjZWVlIi8+PC9zdmc+"}}),e.jsxs("div",{children:[e.jsx("div",{className:"name",children:t.name}),e.jsxs("div",{className:"muted",children:["Qty: ",t.quantity]})]})]}),e.jsx("div",{className:"price",children:a(t.lineTotal,s.totals.currency)})]},t.id))]}),e.jsxs("section",{className:"card summary",children:[e.jsx("h2",{className:"section-title",children:"Summary"}),e.jsxs("div",{className:"row",children:[e.jsx("span",{children:"Subtotal"}),e.jsx("span",{children:a(s.totals.subtotal,s.totals.currency)})]}),e.jsxs("div",{className:"row",children:[e.jsx("span",{children:"Shipping"}),e.jsx("span",{children:a(s.totals.shipping,s.totals.currency)})]}),e.jsxs("div",{className:"row total",children:[e.jsx("span",{children:"Total"}),e.jsx("span",{children:a(s.totals.total,s.totals.currency)})]}),e.jsxs("div",{className:"row",children:[e.jsx("span",{children:"Payment Method"}),e.jsx("span",{children:s.paymentMethod})]}),e.jsxs("div",{className:"actions",children:[e.jsx("button",{className:"btn primary",onClick:x,children:"Download Invoice"}),e.jsx("button",{className:"btn",onClick:p,children:"Share"})]})]})]}),e.jsxs("div",{className:"footer-actions",children:[e.jsx(l,{to:"/orders",className:"btn",children:"Back to Orders"}),e.jsx(l,{to:"/products",className:"btn secondary",children:"Continue Shopping"})]})]})]}):e.jsxs("div",{className:"order-detail-page",children:[e.jsx(h,{}),e.jsx("div",{className:"order-detail-container",children:e.jsxs("div",{className:"not-found",children:[e.jsx("h2",{children:"Order not found"}),e.jsx("p",{children:"Please check the link or view all orders."}),e.jsx(l,{to:"/orders",className:"btn",children:"Back to Orders"})]})})]})}export{b as default};
//# sourceMappingURL=OrderDetailPage-qo2uBU7X.js.map
